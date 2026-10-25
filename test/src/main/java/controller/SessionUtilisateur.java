package controller;

import com.framework.util.Session;
import jakarta.servlet.http.HttpSession;

/**
 * Sprint 9 : exemple de classe Session personnalisee.
 *
 * Cette sous-classe ajoute des raccourcis metier a la Session du framework.
 * Le framework l'instancie automatiquement lorsqu'une methode de controleur
 * declare un parametre de ce type :
 *
 *   public String profil(SessionUtilisateur session) { ... }
 *
 * Contrainte : un constructeur public acceptant HttpSession est obligatoire.
 */
public class SessionUtilisateur extends Session {

    public SessionUtilisateur(HttpSession httpSession) {
        super(httpSession);
    }

    /** true si l'utilisateur connecte a le role admin. */
    public boolean estAdministrateur() {
        return estAdmin();
    }

    /** Initiales de l'utilisateur, pratiques pour un avatar par exemple. */
    public String getInitiales() {
        String nom = getUtilisateur();
        if (nom == null || nom.isBlank()) {
            return "?";
        }
        return nom.trim().substring(0, 1).toUpperCase();
    }
}
