package controller;

import com.framework.annotation.ApiJson;
import com.framework.annotation.AuthentificationRequise;
import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.Authentification;
import com.framework.util.Model;
import com.framework.util.Session;

import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sprint 10 : demonstration des methodes protegees.
 *
 * @AuthentificationRequise fait verifier par le framework que la session
 * contient l'attribut configure dans web.xml (auth.sessionAttribute).
 * Si l'utilisateur n'est pas connecte, la methode ci-dessous n'est jamais
 * appelee : le front controller redirige vers la page de connexion (HTML)
 * ou renvoie une erreur 401 (API JSON).
 */
@controller
public class AdminController {

    /**
     * Page protegee : impossible a afficher sans etre connecte.
     */
    @AuthentificationRequise
    @UrlMapping(url = "/admin", method = "GET")
    public String dashboard(Model model, Session session, HttpServletRequest request) {
        preparerVue(model, session, request);
        return "admin/dashboard";
    }

    /**
     * Action protegee : le compteur prouve que le corps de la methode n'a
     * pas ete execute lorsque l'acces est refuse.
     */
    @AuthentificationRequise
    @UrlMapping(url = "/admin/action", method = "POST")
    public String actionProtegee(Model model, Session session, HttpServletRequest request) {

        int nombre = session.getInt("actionsProtegees", 0) + 1;
        session.setAttribute("actionsProtegees", nombre);
        session.setAttribute("derniereAction", "Action protegee executee");

        preparerVue(model, session, request);
        model.setAttribute("message",
                "Action protegee executee avec succes (" + nombre + " fois dans cette session).");
        return "admin/dashboard";
    }

    /**
     * Version Web API : le refus est renvoye en JSON avec le statut HTTP 401.
     * GET /api/admin
     */
    @ApiJson
    @AuthentificationRequise
    @UrlMapping(url = "/api/admin", method = "GET")
    public Map<String, Object> etatApi(Session session, HttpServletRequest request) {

        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("authentifie", session.estConnecte());
        reponse.put("utilisateur", session.getUtilisateur());
        reponse.put("role", session.getRole());
        reponse.put("admin", session.estAdmin());
        reponse.put("sessionId", session.getId());
        reponse.put("attributControle",
                Authentification.nomAttributConfigure(request.getServletContext()));
        reponse.put("actionsProtegees", session.getInt("actionsProtegees", 0));
        return reponse;
    }

    private void preparerVue(Model model, Session session, HttpServletRequest request) {
        String utilisateur = session.getUtilisateur();

        model.setAttribute("titre", "Sprint 10 - Espace protege");
        model.setAttribute("utilisateur", utilisateur);
        model.setAttribute("role", session.getRole());
        model.setAttribute("admin", session.estAdmin());
        model.setAttribute("sessionId", session.getId());
        model.setAttribute("initiales",
                utilisateur == null || utilisateur.isBlank()
                        ? "?"
                        : utilisateur.trim().substring(0, 1).toUpperCase());
        model.setAttribute("attributControle",
                Authentification.nomAttributConfigure(request.getServletContext()));
        model.setAttribute("actionsProtegees", session.getInt("actionsProtegees", 0));
        model.setAttribute("derniereAction", session.getAttribute("derniereAction"));
    }
}
