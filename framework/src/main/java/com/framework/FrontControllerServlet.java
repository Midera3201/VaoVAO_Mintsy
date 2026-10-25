package com.framework;

import com.framework.util.Authentification;
import com.framework.util.JsonUtil;
import com.framework.util.Mapping;
import com.framework.util.Model;
import com.framework.util.ModelAndView;
import com.framework.util.ParamBinder;
import com.framework.util.UtilMethode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Front controller du framework.
 *
 * @MultipartConfig rend possible la reception des fichiers envoyes par un
 * formulaire avec enctype="multipart/form-data" (Sprint 8) : sans cette
 * annotation, request.getPart() leve une ServletException.
 *
 * Ces valeurs sont des valeurs par defaut. Une application peut les
 * redefinir dans son web.xml, la configuration du descripteur prenant
 * le dessus sur l'annotation :
 *
 *   <multipart-config>
 *       <max-file-size>10485760</max-file-size>       (10 Mo par fichier)
 *       <max-request-size>26214400</max-request-size> (25 Mo au total)
 *       <file-size-threshold>1048576</file-size-threshold>
 *   </multipart-config>
 *
 * Sprint 10 : avant d'invoquer une methode annotee
 * {@code @AuthentificationRequise}, le front controller verifie qu'un
 * utilisateur est authentifie (voir Authentification et ParamBinder).
 */
@MultipartConfig(
        maxFileSize = 10L * 1024 * 1024,
        maxRequestSize = 25L * 1024 * 1024,
        fileSizeThreshold = 1024 * 1024
)
public class FrontControllerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod().toUpperCase();

        HashMap<UtilMethode, Mapping> urlMapping =
                (HashMap<UtilMethode, Mapping>) getServletContext().getAttribute("urlMapping");

        // La page d'accueil est un cas particulier : toujours du HTML
        if (pathInfo.equals("/") || pathInfo.isEmpty()) {
            afficherAccueil(request, response, urlMapping);
            return;
        }

        UtilMethode cle = new UtilMethode(pathInfo, httpMethod);
        Mapping mapping = urlMapping == null ? null : urlMapping.get(cle);

        if (mapping == null) {
            if (estRequeteApi(pathInfo)) {
                ecrireErreurJson(response, HttpServletResponse.SC_NOT_FOUND,
                        "Aucune methode trouvee pour l'URL '" + pathInfo
                                + "' avec la methode HTTP " + httpMethod);
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Aucune methode trouvee pour l'URL '" + pathInfo
                                + "' avec la methode HTTP " + httpMethod);
            }
            return;
        }

        // Sprint 10 : la methode est-elle annotee @AuthentificationRequise ?
        // Le controle a lieu AVANT toute instanciation du controleur, analyse
        // du corps multipart ou invocation de la methode d'action.
        if (!verifierAuthentification(request, response, mapping)) {
            return;
        }

        try {
            Object instance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
            Method method = mapping.getMethod();

            // Sprint 8 : si le corps multipart est inexploitable (fichier trop
            // gros), on s'arrete ici avec un message clair plutot que d'invoquer
            // le controleur avec des arguments tous vides.
            if (!multipartAnalysable(request, response, mapping)) {
                return;
            }

            Model model = new Model();
            Object[] args = buildMethodArgs(method, model, request, response);
            Object resultat = method.invoke(instance, args);

            if (mapping.isJson()) {
                // Web API : on serialise, aucune vue n'est appelee
                ecrireJson(response, resultat, model, HttpServletResponse.SC_OK);
            } else {
                afficherVue(request, response, (String) resultat, model);
            }

        } catch (InvocationTargetException e) {
            // L'exception vient de la methode du controleur, pas de la reflexion
            Throwable cause = e.getCause();
            if (mapping.isJson()) {
                ecrireErreurJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        cause.getMessage() != null ? cause.getMessage() : cause.toString());
            } else {
                throw new ServletException("Erreur lors de l'invocation de la methode " + mapping.getMethod().getName(), cause);
            }
        } catch (Exception e) {
            if (mapping.isJson()) {
                ecrireErreurJson(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            } else {
                throw new ServletException("Erreur lors de l'invocation de la methode " + mapping.getMethod().getName(), e);
            }
        }
    }

    /**
     * Sprint 10 : verifie l'authentification d'une methode protegee.
     *
     * Le nom de l'attribut de session est lu dans la configuration du
     * framework (auth.sessionAttribute), ou dans l'annotation si elle
     * fournit une valeur explicite.
     *
     * @return true si la methode peut etre executee ; false si l'acces a
     *         ete refuse (la reponse a deja ete ecrite ou redirigee)
     */
    private boolean verifierAuthentification(HttpServletRequest request,
                                             HttpServletResponse response,
                                             Mapping mapping) throws IOException {

        Method method = mapping.getMethod();

        if (Authentification.estAuthentifie(request, method)) {
            return true;
        }

        String attribut = Authentification.nomAttribut(request, method);

        ParamBinder.signaler(request, "Sprint 10 : acces refuse a ["
                + request.getMethod().toUpperCase() + "] " + request.getRequestURI()
                + " -> " + mapping.getControllerClass().getSimpleName()
                + "." + method.getName() + "(...) : attribut de session '"
                + attribut + "' absent, vide ou false");

        String message = Authentification.messageRefus();

        // Web API : le client doit pouvoir traiter le refus en JSON.
        if (mapping.isJson()) {
            ecrireErreurJson(response, HttpServletResponse.SC_UNAUTHORIZED, message);
            return false;
        }

        // Page HTML : redirection vers la page de connexion si configuree.
        String loginUrl = Authentification.loginUrl(getServletContext());
        if (loginUrl != null) {
            response.sendRedirect(
                    Authentification.construireUrlConnexion(request, loginUrl));
            return false;
        }

        // Sinon, refus explicite en HTTP 401.
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        return false;
    }

    /**
     * Sprint 8 : verifie que le corps multipart est analysable AVANT d'invoquer
     * le controleur.
     *
     * Des qu'un fichier depasse max-file-size, ou que le total depasse
     * max-request-size, le conteneur refuse la requete. Dans ce cas les
     * fichiers mais AUSSI tous les champs texte deviennent indisponibles :
     * sans ce controle, le controleur recevrait un Employe vide et
     * afficherait un message trompeur. On rend donc une erreur explicite.
     *
     * @return false si la requete a ete rejetee (l'erreur a deja ete ecrite)
     */
    private boolean multipartAnalysable(HttpServletRequest request, HttpServletResponse response,
                                        Mapping mapping) throws IOException {

        if (!ParamBinder.estMultipart(request)) {
            return true;
        }

        try {
            // Force l'analyse du corps : c'est ici que les depassements
            // de taille sont detectes par le conteneur.
            request.getParts();
            return true;

        } catch (Exception e) {
            String message = "Fichier refuse par le conteneur : un fichier depasse "
                    + "la taille maximale autorisee, ou la requete est trop volumineuse"
                    + (e.getMessage() != null ? " (" + e.getMessage() + ")" : "")
                    + ". Limites configurables dans web.xml (<multipart-config>).";

            ParamBinder.signaler(request, "Requete multipart rejetee sur " + request.getRequestURI()
                    + " -> " + message);

            if (mapping.isJson()) {
                ecrireErreurJson(response, HttpServletResponse.SC_BAD_REQUEST, message);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, message);
            }
            return false;
        }
    }

    /**
     * Chemin actuel du framework : le resultat est un nom de vue,
     * on le combine au prefix/suffix puis on forward vers le JSP.
     */
    private void afficherVue(HttpServletRequest request, HttpServletResponse response,
                             String viewName, Model model)
            throws ServletException, IOException {

        if (viewName == null) {
            throw new ServletException("La methode a retourne null : un nom de vue est attendu");
        }

        response.setContentType("text/html;charset=UTF-8");

        ModelAndView mav = new ModelAndView(viewName);
        for (Map.Entry<String, Object> entry : model.getData().entrySet()) {
            mav.addObject(entry.getKey(), entry.getValue());
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        String prefix = (String) getServletContext().getAttribute("viewPrefix");
        String suffix = (String) getServletContext().getAttribute("viewSuffix");

        String viewPath = (prefix != null ? prefix : "") + viewName + (suffix != null ? suffix : "");
        request.getRequestDispatcher(viewPath).forward(request, response);
    }

    /**
     * Sortie JSON : on ecrit le resultat directement dans la reponse.
     * Si la methode a utilise un Model et renvoye null, c'est le Model
     * qui est serialise.
     */
    private void ecrireJson(HttpServletResponse response, Object resultat, Model model, int status)
            throws IOException {

        Object aSerialiser = resultat;
        if (aSerialiser == null && !model.getData().isEmpty()) {
            aSerialiser = model.getData();
        }

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(JsonUtil.toJson(aSerialiser));
    }

    private void ecrireErreurJson(HttpServletResponse response, int status, String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                "{\"erreur\":" + JsonUtil.toJson(message != null ? message : "Erreur interne") + "}");
    }

    /**
     * Convention : toute URL commencant par /api est consideree comme
     * une requete d'API, y compris lorsqu'aucune route ne correspond,
     * afin de renvoyer du JSON plutot qu'une page d'erreur HTML.
     */
    private boolean estRequeteApi(String pathInfo) {
        return pathInfo.startsWith("/api/") || pathInfo.equals("/api");
    }

    private void afficherAccueil(HttpServletRequest request, HttpServletResponse response,
                                 HashMap<UtilMethode, Mapping> urlMapping)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<h1>Bienvenue sur le Framework</h1>");

        if (urlMapping == null || urlMapping.isEmpty()) {
            out.println("<p style='color:red'>Aucune route enregistree.</p>");
            return;
        }

        out.println("<style>"
                + "table{border-collapse:collapse;font-family:Segoe UI,Arial,sans-serif;margin:16px 0}"
                + "th,td{border:1px solid #ccc;padding:8px 14px;text-align:left}"
                + "th{background:#2c3e50;color:#fff}"
                + "tr:nth-child(even){background:#f7f9fb}"
                + ".m{display:inline-block;min-width:58px;font-weight:bold}"
                + ".get{color:#1a7f37}.post{color:#b5541c}.put{color:#1750ab}.delete{color:#b31d28}"
                + ".t{font-size:12px;padding:2px 8px;border-radius:10px;white-space:nowrap}"
                + ".tv{background:#e8eef7;color:#1750ab}.tj{background:#e6f6ec;color:#1a7f37}"
                + "</style>");

        out.println("<h2>Routes enregistrees</h2>");
        out.println("<table><tr><th>HTTP</th><th>URL</th><th>Type</th></tr>");

        boolean aUneApi = false;
        for (Map.Entry<UtilMethode, Mapping> entry : urlMapping.entrySet()) {
            UtilMethode cle = entry.getKey();
            boolean json = entry.getValue().isJson();
            if (json) {
                aUneApi = true;
            }

            String methode = cle.getHttpMethod();
            String classe = methode.equals("GET") ? "get"
                    : methode.equals("POST") ? "post"
                    : methode.equals("PUT") ? "put" : "delete";
            String context = request.getContextPath();

            out.println("<tr>"
                    + "<td><span class='m " + classe + "'>" + methode + "</span></td>"
                    + "<td>" + (methode.equals("GET")
                            ? "<a href='" + context + cle.getUrl() + "'>" + cle.getUrl() + "</a>"
                            : "<span>" + cle.getUrl() + "</span>") + "</td>"
                    + "<td><span class='t " + (json ? "tj'>JSON" : "tv'>vue") + "</span></td>"
                    + "</tr>");
        }
        out.println("</table>");

        if (aUneApi) {
            out.println("<p><a href='" + request.getContextPath() + "/api-test'>"
                    + "&#9654; Page de test de la Web API (boutons GET / POST)</a></p>");
        }
        out.println("<p style='color:#666;font-size:13px'>"
                + "Les routes GET sont cliquables. "
                + "Les routes POST ne peuvent pas etre ouvertes dans la barre d'adresse "
                + "du navigateur : utilise la page de test ci-dessus.</p>");
    }

    /**
     * Sprint 7 : la construction des arguments est deleguee a ParamBinder.
     * Celui-ci injecte les objets du framework (Model, requete, reponse...)
     * et lie les autres parametres aux champs du formulaire par leur nom.
     */
    private Object[] buildMethodArgs(Method method, Model model, HttpServletRequest request, HttpServletResponse response) {
        return ParamBinder.bind(method, model, request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
