package com.framework.util;

import com.framework.annotation.AuthentificationRequise;
import com.framework.annotation.RolesAutorises;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/**
 * Sprint 10 : verification de l'authentification d'une methode de controleur.
 * Sprint 10 bis : verification des roles autorises.
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
 * Le role est lu dans un autre attribut de session, configurable avec
 * auth.roleAttribute (valeur par defaut : "role"). L'annotation
 * {@link RolesAutorises} accepte plusieurs roles et implique l'authentification.
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

    /** Parametre web.xml : nom de l'attribut de session contenant le role. */
    public static final String PARAM_ATTRIBUT_ROLE = "auth.roleAttribute";

    /** Attribut du ServletContext contenant le nom configure. */
    public static final String CONTEXT_ATTRIBUT_SESSION = "auth.sessionAttribute";

    /** Attribut du ServletContext contenant l'URL de connexion. */
    public static final String CONTEXT_LOGIN_URL = "auth.loginUrl";

    /** Attribut du ServletContext contenant le nom de l'attribut de role. */
    public static final String CONTEXT_ATTRIBUT_ROLE = "auth.roleAttribute";

    /** Valeur utilisee si aucune configuration n'est fournie. */
    public static final String ATTRIBUT_PAR_DEFAUT = Session.ATTRIBUT_UTILISATEUR;

    /** Valeur par defaut de l'attribut contenant le role. */
    public static final String ATTRIBUT_ROLE_PAR_DEFAUT = Session.ATTRIBUT_ROLE;

    private static final String MESSAGE_REFUS =
            "Authentification requise : vous devez etre connecte pour acceder a cette ressource.";

    private static final String MESSAGE_ROLE_REFUS =
            "Acces refuse : votre role ne permet pas d'executer cette action.";

    private Authentification() {
    }

    /**
     * true si la methode exige une authentification : annotation
     * {@link AuthentificationRequise} ou {@link RolesAutorises}.
     */
    public static boolean estRequise(Method method) {
        return method != null
                && (method.isAnnotationPresent(AuthentificationRequise.class)
                    || method.isAnnotationPresent(RolesAutorises.class));
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
     * Lit le nom de l'attribut de session qui contient le role de
     * l'utilisateur. Configuration : auth.roleAttribute, sinon "role".
     */
    public static String nomAttributRoleConfigure(ServletContext context) {
        if (context != null) {
            Object valeur = context.getAttribute(CONTEXT_ATTRIBUT_ROLE);
            if (valeur instanceof String texte && !texte.isBlank()) {
                return texte.trim();
            }
            String parametre = context.getInitParameter(PARAM_ATTRIBUT_ROLE);
            if (parametre != null && !parametre.isBlank()) {
                return parametre.trim();
            }
        }
        return ATTRIBUT_ROLE_PAR_DEFAUT;
    }

    /** true si la methode porte l'annotation {@link RolesAutorises}. */
    public static boolean estRoleRequis(Method method) {
        return method != null && method.isAnnotationPresent(RolesAutorises.class);
    }

    /**
     * Roles autorises par l'annotation, normalises en majuscules.
     * Retourne un ensemble vide si la methode n'est pas annotee.
     */
    public static Set<String> rolesAutorises(Method method) {
        if (method == null) {
            return Collections.emptySet();
        }
        RolesAutorises annotation = method.getAnnotation(RolesAutorises.class);
        if (annotation == null) {
            return Collections.emptySet();
        }

        Set<String> roles = new LinkedHashSet<>();
        for (String role : annotation.value()) {
            ajouterRole(roles, role);
        }
        return Collections.unmodifiableSet(roles);
    }

    /**
     * Roles de l'utilisateur connecte, lus dans l'attribut de session
     * configure (auth.roleAttribute). Accepte une chaine simple, une chaine
     * avec plusieurs roles separes par une virgule ou un point-virgule,
     * une collection, un tableau ou une constante d'enum.
     *
     * Exemples :
     *   session.setAttribute("role", "ADMIN");
     *   session.setAttribute("role", "ADMIN,MANAGER");
     *   session.setAttribute("role", List.of("ADMIN", "MANAGER"));
     */
    public static Set<String> rolesUtilisateur(HttpServletRequest request) {
        if (request == null) {
            return Collections.emptySet();
        }
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            return Collections.emptySet();
        }
        String attribut = nomAttributRoleConfigure(request.getServletContext());
        return extraireRoles(httpSession.getAttribute(attribut));
    }

    /**
     * true si la methode n'est pas annotee @RolesAutorises, ou si l'utilisateur
     * possede au moins un des roles demandes.
     *
     * L'authentification est verifiee separement par estAuthentifie().
     */
    public static boolean aUnRoleAutorise(HttpServletRequest request, Method method) {
        if (!estRoleRequis(method)) {
            return true;
        }

        Set<String> autorises = rolesAutorises(method);
        if (autorises.isEmpty()) {
            // Annotation presente sans aucun role : personne n'est autorise.
            return false;
        }

        for (String roleUtilisateur : rolesUtilisateur(request)) {
            if (autorises.contains(roleUtilisateur)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extrait et normalise les roles d'une valeur de session.
     * Les elements imbriques (collection, tableau, Optional) sont acceptes.
     */
    private static Set<String> extraireRoles(Object valeur) {
        Set<String> roles = new LinkedHashSet<>();
        collecterRoles(valeur, roles, 0);
        return Collections.unmodifiableSet(roles);
    }

    private static void collecterRoles(Object valeur, Set<String> roles, int profondeur) {
        // Garde-fou contre les structures imbriquees anormalement profondes
        if (valeur == null || profondeur > 5) {
            return;
        }

        if (valeur instanceof CharSequence texte) {
            for (String partie : texte.toString().split("[,;]")) {
                ajouterRole(roles, partie);
            }
            return;
        }

        if (valeur instanceof Optional<?> optionnel) {
            if (optionnel.isPresent()) {
                collecterRoles(optionnel.get(), roles, profondeur + 1);
            }
            return;
        }

        if (valeur instanceof Collection<?> collection) {
            for (Object item : collection) {
                collecterRoles(item, roles, profondeur + 1);
            }
            return;
        }

        if (valeur.getClass().isArray()) {
            int longueur = Array.getLength(valeur);
            for (int i = 0; i < longueur; i++) {
                collecterRoles(Array.get(valeur, i), roles, profondeur + 1);
            }
            return;
        }

        if (valeur instanceof Enum<?> enumeration) {
            ajouterRole(roles, enumeration.name());
            return;
        }

        ajouterRole(roles, String.valueOf(valeur));
    }

    private static void ajouterRole(Set<String> roles, String role) {
        if (role == null) {
            return;
        }
        String nettoye = role.trim();
        if (!nettoye.isEmpty()) {
            roles.add(nettoye.toUpperCase(Locale.ROOT));
        }
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

    /** Message generique renvoye lorsque le role n'est pas autorise. */
    public static String messageRoleRefus() {
        return MESSAGE_ROLE_REFUS;
    }
}
