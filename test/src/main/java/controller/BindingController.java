package controller;

import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.Model;

/**
 * Sprint 7 : demonstration du binding automatique des parametres HTTP.
 *
 * La methode resultat() ne lit AUCUN request.getParameter() :
 * le framework remplit ses arguments par reflexion, par nom.
 */
@controller
public class BindingController {

    @UrlMapping(url = "/binding", method = "GET")
    public String formulaire(Model model) {
        model.setAttribute("titre", "Sprint 7 - Binding des parametres");
        return "binding/form";
    }

    /**
     * Meme forme que l'exemple du sprint : save(String nom, int age).
     * Le nom des arguments correspond au name="..." des champs HTML.
     */
    @UrlMapping(url = "/binding/resultat", method = "GET")
    public String resultat(Model model, String nom, int age, double taille, boolean actif) {
        model.setAttribute("nom", nom);
        model.setAttribute("age", age);
        model.setAttribute("taille", taille);
        model.setAttribute("actif", actif);
        model.setAttribute("ageAbsent", age == 0);
        return "binding/resultat";
    }

    /**
     * Version POST : le binding fonctionne a l'identique, que les donnees
     * viennent de la query string (GET) ou du corps du formulaire (POST).
     */
    @UrlMapping(url = "/binding/resultat", method = "POST")
    public String resultatPost(Model model, String nom, int age) {
        model.setAttribute("nom", nom);
        model.setAttribute("age", age);
        model.setAttribute("taille", 0.0);
        model.setAttribute("actif", false);
        model.setAttribute("ageAbsent", age == 0);
        return "binding/resultat";
    }
}
