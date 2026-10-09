package controller;

import com.framework.annotation.ApiJson;
import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.Model;
import repository.EmpRepository;

import jakarta.servlet.http.HttpServletRequest;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@controller
public class EmpApiController {

    private final EmpRepository repo = new EmpRepository();

    @ApiJson
    @UrlMapping(url = "/api/employes", method = "GET")
    public List<Employe> liste() throws Exception {
        return repo.findAll();
    }

    @ApiJson
    @UrlMapping(url = "/api/employes", method = "POST")
    public Map<String, Object> creer(HttpServletRequest request) throws Exception {
        Map<String, Object> reponse = new LinkedHashMap<>();
        String nom = request.getParameter("nom");
        String prenom = request.getParameter("prenom");
        String email = request.getParameter("email");

        if (nom == null || nom.isBlank() || prenom == null || prenom.isBlank()) {
            reponse.put("succes", false);
            reponse.put("message", "nom et prenom sont obligatoires");
            return reponse;
        }

        Employe e = new Employe();
        e.setNom(nom.trim());
        e.setPrenom(prenom.trim());
        e.setEmail(email != null ? email.trim() : null);
        repo.save(e);

        reponse.put("succes", true);
        reponse.put("message", "Employe enregistre");
        reponse.put("employe", e);
        return reponse;
    }

    @ApiJson
    @UrlMapping(url = "/api/employes/supprimer", method = "POST")
    public Map<String, Object> supprimer(HttpServletRequest request) throws Exception {
        Map<String, Object> reponse = new LinkedHashMap<>();
        String id = request.getParameter("id");

        if (id == null) {
            reponse.put("succes", false);
            reponse.put("message", "parametre id manquant");
            return reponse;
        }

        repo.delete(Integer.parseInt(id));
        reponse.put("succes", true);
        reponse.put("message", "Employe " + id + " supprime");
        return reponse;
    }

    @ApiJson
    @UrlMapping(url = "/api/stats", method = "GET")
    public Map<String, Object> stats(Model model) throws Exception {
        int total = repo.findAll().size();
        model.setAttribute("total", total);
        // La methode renvoie null : c'est le Model qui sera serialise
        return null;
    }

    @ApiJson
    @UrlMapping(url = "/api/salama", method = "GET")
    public String test() {
        
        return "binjour";
    }
}
    