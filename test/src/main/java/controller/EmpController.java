package controller;

import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.Model;

import jakarta.servlet.http.HttpServletRequest;

import repository.EmpRepository;
import java.util.List;

@controller
public class EmpController {

    @UrlMapping(url = "/emp/list/", method = "GET")
    public String empListe(Model model) {
        try {
            EmpRepository repo = new EmpRepository();
            List<Employe> employes = repo.findAll();
            model.setAttribute("employes", employes);
            model.setAttribute("titre", "Liste des employes");
        } catch (Exception e) {
            model.setAttribute("erreur", e.getMessage());
        }
        return "emp/list";
    }

    @UrlMapping(url = "/emp/list/", method = "POST")
    public String empListePost(Model model, HttpServletRequest request) {
        try {
            String nom = request.getParameter("nom");
            String prenom = request.getParameter("prenom");
            String email = request.getParameter("email");

            if (nom != null && prenom != null) {
                Employe e = new Employe();
                e.setNom(nom);
                e.setPrenom(prenom);
                e.setEmail(email);

                EmpRepository repo = new EmpRepository();
                repo.save(e);
            }

            EmpRepository repo = new EmpRepository();
            List<Employe> employes = repo.findAll();
            model.setAttribute("employes", employes);
            model.setAttribute("titre", "Liste des employes");
        } catch (Exception e) {
            model.setAttribute("erreur", e.getMessage());
        }
        return "emp/list";
    }

    @UrlMapping(url = "/emp/new")
    public String empNew(Model model) {
        model.setAttribute("titre", "Nouvel employe");
        return "emp/new";
    }

    @UrlMapping(url = "/andrana")
    public String andrana(Model model) {
        model.setAttribute("titre", "Page de test");
        model.setAttribute("message", "Bienvenue sur la page de test");
        return "andrana";
    }
}
