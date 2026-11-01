package controller;

import com.framework.annotation.ApiJson;
import com.framework.annotation.RolesAutorises;
import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.Authentification;
import com.framework.util.Model;
import com.framework.util.Session;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sprint 10 bis : demonstration du controle d'acces par role.
 *
 * @RolesAutorises implique l'authentification : inutile d'ajouter
 * @AuthentificationRequise. Le framework verifie le role AVANT d'appeler
 * la methode. Un utilisateur connecte sans le bon role recoit un HTTP 403.
 */
@controller
public class RoleController {

    /**
     * Gestion des utilisateurs : reservee au role ADMIN.
     */
    @RolesAutorises({"ADMIN"})
    @UrlMapping(url = "/admin/utilisateurs", method = "GET")
    public String utilisateurs(Model model, Session session, HttpServletRequest request) {
        preparerVue(model, session, request, "ADMIN");
        model.setAttribute("utilisateurs", utilisateursDemo());
        return "role/utilisateurs";
    }

    /**
     * Espace personnel : reserve au role USER.
     */
    @RolesAutorises({"USER"})
    @UrlMapping(url = "/mon-espace", method = "GET")
    public String monEspace(Model model, Session session, HttpServletRequest request) {
        preparerVue(model, session, request, "USER");
        model.setAttribute("message", "Bienvenue dans votre espace personnel.");
        return "role/mon-espace";
    }

    /**
     * Espace accessible a plusieurs roles : ADMIN ou USER.
     * Illustre l'acceptation de plusieurs roles pour une meme methode.
     */
    @RolesAutorises({"ADMIN", "USER"})
    @UrlMapping(url = "/espace-partage", method = "GET")
    public String espacePartage(Model model, Session session, HttpServletRequest request) {
        preparerVue(model, session, request, "ADMIN, USER");
        model.setAttribute("message", "Cette page accepte les roles ADMIN et USER.");
        return "role/partage";
    }

    /**
     * API reservee a ADMIN : un USER authentifie recoit un JSON 403.
     * GET /api/admin/utilisateurs
     */
    @ApiJson
    @RolesAutorises({"ADMIN"})
    @UrlMapping(url = "/api/admin/utilisateurs", method = "GET")
    public Map<String, Object> utilisateursApi(Session session) {
        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("role", session.getRole());
        reponse.put("autorise", true);
        reponse.put("utilisateurs", utilisateursDemo());
        return reponse;
    }

    private void preparerVue(Model model, Session session, HttpServletRequest request,
                             String rolesAutorises) {
        model.setAttribute("titre", "Sprint 10 bis - Controle des roles");
        model.setAttribute("utilisateur", session.getUtilisateur());
        model.setAttribute("role", session.getRole());
        model.setAttribute("admin", session.estAdmin());
        model.setAttribute("sessionId", session.getId());
        model.setAttribute("rolesAutorises", rolesAutorises);
        model.setAttribute("attributRole",
                Authentification.nomAttributRoleConfigure(request.getServletContext()));
    }

    /**
     * Liste fictive : aucune base de donnees n'est necessaire pour cette
     * demonstration du controle d'acces.
     */
    private List<Map<String, Object>> utilisateursDemo() {
        List<Map<String, Object>> utilisateurs = new ArrayList<>();

        Map<String, Object> premier = new LinkedHashMap<>();
        premier.put("id", 1);
        premier.put("nom", "Midera");
        premier.put("email", "midera@test.mg");
        premier.put("role", "ADMIN");
        utilisateurs.add(premier);

        Map<String, Object> deuxieme = new LinkedHashMap<>();
        deuxieme.put("id", 2);
        deuxieme.put("nom", "Naivo");
        deuxieme.put("email", "naivo@test.mg");
        deuxieme.put("role", "USER");
        utilisateurs.add(deuxieme);

        Map<String, Object> troisieme = new LinkedHashMap<>();
        troisieme.put("id", 3);
        troisieme.put("nom", "Rakoto");
        troisieme.put("email", "rakoto@test.mg");
        troisieme.put("role", "USER");
        utilisateurs.add(troisieme);

        return utilisateurs;
    }
}
