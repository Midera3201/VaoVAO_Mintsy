package controller;

import com.framework.annotation.ApiJson;
import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.Model;
import com.framework.util.Session;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Sprint 9 : demonstration de la gestion des sessions.
 *
 * Aucune methode ne manipule HttpSession directement : le framework injecte
 * un objet Session (ou la sous-classe SessionUtilisateur) dans les methodes
 * qui le declarent. Les donnees sont conservees par le conteneur entre les
 * requetes d'un meme navigateur (cookie JSESSIONID).
 */
@controller
public class SessionController {

    private static final String VUE_INDEX = "session/index";
    private static final String VUE_PROFIL = "session/profil";

    // ------------------------------------------------------------------
    // Pages HTML
    // ------------------------------------------------------------------

    @UrlMapping(url = "/session", method = "GET")
    public String accueil(Model model, Session session) {
        model.setAttribute("titre", "Sprint 9 - Gestion des sessions");
        preparerEtat(model, session);
        return VUE_INDEX;
    }

    /**
     * Ajoute l'utilisateur connecte dans la session.
     * Le parametre Session est fourni par le framework : pas de
     * request.getSession() ni de HttpSession dans ce controleur.
     */
    @UrlMapping(url = "/session/connexion", method = "POST")
    public String connexion(Model model, Session session, String nom, String role) {

        if (estVide(nom)) {
            model.setAttribute("titre", "Sprint 9 - Gestion des sessions");
            model.setAttribute("erreur", "Le nom de l'utilisateur est obligatoire.");
            preparerEtat(model, session);
            return VUE_INDEX;
        }

        session.connecter(nom.trim(), role);
        session.setAttribute("connexionLe", new Date());

        preparerProfil(model, session);
        model.setAttribute("message",
                "Bienvenue " + session.getUtilisateur()
                        + " ! Les donnees sont enregistrees dans la session.");
        return VUE_PROFIL;
    }

    /**
     * Relecture des donnees lors d'une NOUVELLE requete HTTP.
     * Ici le parametre est la sous-classe personnalisee SessionUtilisateur :
     * ParamBinder l'instancie a partir de la HttpSession existante.
     */
    @UrlMapping(url = "/session/profil", method = "GET")
    public String profil(Model model, SessionUtilisateur session) {
        preparerProfil(model, session);
        return VUE_PROFIL;
    }

    /** Modification de l'utilisateur et de son role. */
    @UrlMapping(url = "/session/modifier", method = "POST")
    public String modifier(Model model, Session session, String nom, String role) {

        if (estVide(nom)) {
            preparerProfil(model, session);
            model.setAttribute("erreur", "Le nom de l'utilisateur est obligatoire.");
            return VUE_PROFIL;
        }

        session.setUtilisateur(nom.trim());
        session.setRole(role);

        preparerProfil(model, session);
        model.setAttribute("message", "Les informations de la session ont ete modifiees.");
        return VUE_PROFIL;
    }

    /**
     * Ajout ou modification d'une donnee libre : setAttribute remplace la
     * valeur si la cle existe deja.
     */
    @UrlMapping(url = "/session/attribut", method = "POST")
    public String definirAttribut(Model model, Session session, String cle, String valeur) {

        if (estVide(cle)) {
            preparerProfil(model, session);
            model.setAttribute("erreur", "La cle de l'attribut est obligatoire.");
            return VUE_PROFIL;
        }

        session.setAttribute(cle.trim(), valeur);

        preparerProfil(model, session);
        model.setAttribute("message", "Attribut « " + cle.trim() + " » enregistre dans la session.");
        return VUE_PROFIL;
    }

    /** Suppression d'une donnee de session. */
    @UrlMapping(url = "/session/attribut/supprimer", method = "POST")
    public String supprimerAttribut(Model model, Session session, String cle) {

        if (estVide(cle)) {
            preparerProfil(model, session);
            model.setAttribute("erreur", "Aucun attribut a supprimer.");
            return VUE_PROFIL;
        }

        session.removeAttribute(cle.trim());

        preparerProfil(model, session);
        model.setAttribute("message", "Attribut « " + cle.trim() + " » supprime de la session.");
        return VUE_PROFIL;
    }

    /**
     * Compteur de visites : la valeur est incrementee a chaque appel.
     * Recharger la page prouve que la donnee survit d'une requete a l'autre.
     */
    @UrlMapping(url = "/session/compteur", method = "GET")
    public String compteur(Model model, Session session) {

        int visites = session.getInt("visites", 0) + 1;
        session.setAttribute("visites", visites);

        model.setAttribute("titre", "Sprint 9 - Compteur de visites");
        model.setAttribute("visites", visites);
        model.setAttribute("sessionId", session.getId());
        model.setAttribute("connecte", session.estConnecte());
        model.setAttribute("utilisateur", session.getUtilisateur());
        return "session/compteur";
    }

    /** Deconnexion : detruit la session et toutes les donnees associees. */
    @UrlMapping(url = "/session/deconnexion", method = "POST")
    public String deconnexion(Model model, Session session) {

        String utilisateur = session.getUtilisateur();
        session.invalidate();

        model.setAttribute("titre", "Sprint 9 - Deconnexion");
        model.setAttribute("message", utilisateur == null
                ? "La session a ete detruite."
                : utilisateur + " a ete deconnecte et la session a ete detruite.");
        return "session/deconnecte";
    }

    // ------------------------------------------------------------------
    // Version JSON (Sprint 6) : la session fonctionne aussi avec @ApiJson
    // ------------------------------------------------------------------

    @ApiJson
    @UrlMapping(url = "/api/session", method = "GET")
    public Map<String, Object> etatJson(Session session) {

        Map<String, Object> reponse = new LinkedHashMap<>();
        reponse.put("sessionId", session.getId());
        reponse.put("nouvelle", session.estNouvelle());
        reponse.put("connecte", session.estConnecte());
        reponse.put("utilisateur", session.getUtilisateur());
        reponse.put("role", session.getRole());
        reponse.put("admin", session.estAdmin());
        reponse.put("attributs", session.getDonnees());
        reponse.put("dureeInactiviteSecondes", session.getDureeInactivite());
        return reponse;
    }

    // ------------------------------------------------------------------
    // Preparation des donnees pour les vues
    // ------------------------------------------------------------------

    private void preparerEtat(Model model, Session session) {
        model.setAttribute("sessionId", session.getId());
        model.setAttribute("nouvelle", session.estNouvelle());
        model.setAttribute("connecte", session.estConnecte());
        model.setAttribute("utilisateur", session.getUtilisateur());
        model.setAttribute("role", session.getRole());
        model.setAttribute("admin", session.estAdmin());
        model.setAttribute("nbAttributs", session.getDonnees().size());
        model.setAttribute("dureeInactivite", session.getDureeInactivite());
    }

    private void preparerProfil(Model model, Session session) {
        preparerEtat(model, session);

        model.setAttribute("titre", "Sprint 9 - Profil de session");
        model.setAttribute("attributs", session.getDonnees());
        model.setAttribute("creation",
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        .format(new Date(session.getDateCreation())));
        model.setAttribute("dernierAcces",
                new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        .format(new Date(session.getDateDernierAcces())));

        String nom = session.getUtilisateur();
        model.setAttribute("initiales",
                nom == null || nom.isBlank() ? "?" : nom.trim().substring(0, 1).toUpperCase());
        model.setAttribute("typeSession", session.getClass().getSimpleName());
    }

    private static boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
