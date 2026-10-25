package com.framework.util;

import com.framework.annotation.AuthentificationRequise;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Sprint 10 : verification de l'authentification d'une methode de controleur.
 *
 * Le nom de l'attribut de session qui indique qu'un utilisateur est authentifie
 * est configurable dans web.xml :
 *
 *   <context-param>
 *       <param-name>auth.sessionAttribute</param-name>
 *       <param-value>utilisateur</param-value>
 *   </context-param>
 *
 * Il peut aussi etre defini par methode avec
 * {@code @AuthentificationRequise(attribut = "...")}.
 *
 * Un attribut est considere comme authentifie lorsqu'il existe et que sa valeur
 * n'est ni null, ni false, ni une chaine vide. Ainsi, les usages suivants
 * fonctionnent :
 *
 *   session.setAttribute("utilisateur", "Midera");  -> authentifie
 *   session.setAttribute("connecte", true);         -> authentifie
 *   session.setAttribute("utilisateur", "");        -> refuse
 *   session.setAttribute("connecte", false);        -> refuse
 *   attribut absent                                 -> refuse
 */
public final class Authentification {

    /** Parametre web.xml : nom de l'attribut de session a controler. */
    public static final String PARAM_ATTRIBUT_SESSION = "auth.sessionAttribute";

    /** Parametre web.xml : page vers laquelle rediriger un visiteur refuse. */
    public static final String PARAM_LOGIN_URL = "auth.loginUrl";

    /** Attribut du ServletContext contenant le nom configure. */
    public static final String CONTEXT_ATTRIBUT_SESSION = "auth.sessionAttribute";

    /** Attribut du ServletContext contenant l'URL de connexion. */
    public static final String CONTEXT_LOGIN_URL = "auth.loginUrl";

    /** Valeur utilisee si aucune configuration n'est fournie. */
    public static final String ATTRIBUT_PAR_DEFAUT = Session.ATTRIBUT_UTILISATEUR;

    private static final String MESSAGE_REFUS =
            "Authentification requise : vous devez etre connecte pour acceder a cette ressource.";

    private Authentification() {
    }

    /**
     * true si la methode est annotee {@link AuthentificationRequise}.
     */
    public static boolean estRequise(Method method) {
        return method != null && method.isAnnotationPresent(AuthentificationRequise.class);
    }

    /**
     * true si la methode n'est pas protegee, ou si la session contient une
     * valeur authentifiee dans l'attribut attendu.
     */
    public static boolean estAuthentifie(HttpServletRequest request, Method method) {
        if (!estRequise(method)) {
            return true;
        }
        // getSession(false) : on ne cree pas de session pour un visiteur
        // anonyme qui tente d'atteindre une methode protegee.
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            return false;
        }
        return estValeurAuthentifiee(httpSession.getAttribute(nomAttribut(request, method)));
    }

    /**
     * Nom de l'attribut de session effectivement controle :
     * valeur de l'annotation si elle est renseignee, sinon configuration
     * globale, sinon valeur par defaut.
     */
    public static String nomAttribut(HttpServletRequest request, Method method) {
        if (method != null) {
            AuthentificationRequise annotation =
                    method.getAnnotation(AuthentificationRequise.class);
            if (annotation != null
                    && annotation.attribut() != null
                    && !annotation.attribut().isBlank()) {
                return annotation.attribut().trim();
            }
        }
        return nomAttributConfigure(request != null ? request.getServletContext() : null);
    }

    /**
     * Lit le nom configure dans le ServletContext. Le listener y copie la
     * valeur de web.xml au demarrage ; on lit aussi le parametre directement
     * pour rester utilisable si le listener n'a pas ete execute.
     */
    public static String nomAttributConfigure(ServletContext context) {
        if (context != null) {
            Object valeur = context.getAttribute(CONTEXT_ATTRIBUT_SESSION);
            if (valeur instanceof String texte && !texte.isBlank()) {
                return texte.trim();
            }
            String parametre = context.getInitParameter(PARAM_ATTRIBUT_SESSION);
            if (parametre != null && !parametre.isBlank()) {
                return parametre.trim();
            }
        }
        return ATTRIBUT_PAR_DEFAUT;
    }

    /**
     * URL de connexion configuree, ou null si aucune redirection n'est
     * souhaitee (le framework renvoie alors une erreur HTTP 401).
     */
    public static String loginUrl(ServletContext context) {
        if (context != null) {
            Object valeur = context.getAttribute(CONTEXT_LOGIN_URL);
            if (valeur instanceof String texte && !texte.isBlank()) {
                return texte.trim();
            }
            String parametre = context.getInitParameter(PARAM_LOGIN_URL);
            if (parametre != null && !parametre.isBlank()) {
                return parametre.trim();
            }
        }
        return null;
    }

    /**
     * Construit l'URL de redirection dans le contexte de l'application.
     * Une valeur "session" ou "/session" devient "/<contexte>/session".
     */
    public static String construireUrlConnexion(HttpServletRequest request, String loginUrl) {
        if (loginUrl == null || loginUrl.isBlank()) {
            return null;
        }
        String url = loginUrl.trim();
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        return request.getContextPath() + url;
    }

    /**
     * Interpretation de la valeur stockee en session.
     * Seuls null, false et les chaines vides/blanches refusent l'acces.
     */
    public static boolean estValeurAuthentifiee(Object valeur) {
        if (valeur == null) {
            return false;
        }
        if (valeur instanceof Boolean booleen) {
            return booleen;
        }
        if (valeur instanceof CharSequence texte) {
            return !texte.toString().isBlank();
        }
        if (valeur instanceof Optional<?> optionnel) {
            return optionnel.isPresent() && estValeurAuthentifiee(optionnel.get());
        }
        return true;
    }

    /** Message generique renvoye au client lorsque l'acces est refuse. */
    public static String messageRefus() {
        return MESSAGE_REFUS;
    }
}
