package controller;

import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.FileStorage;
import com.framework.util.FileStorageException;
import com.framework.util.MapFile;
import com.framework.util.Model;

import jakarta.servlet.http.HttpServletRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Sprint 8 : reception de fichiers envoyes depuis un formulaire HTML
 * en multipart/form-data.
 *
 * Le controleur ne fait AUCUN request.getPart() :
 * le framework lit les parts, construit les objets MapFile et les
 * transmet aux parametres en les associant par nom de champ HTML.
 *
 *   <input type="file" name="rectoCIN">  ->  MapFile rectoCIN
 *   <input type="file" name="versoCIN">  ->  MapFile versoCIN
 *   <input type="text" name="nom">       ->  employe.setNom(...)
 *
 * La recuperation du fichier (role du framework) et son stockage
 * (role du controleur, via FileStorage) sont deux operations distinctes.
 */
@controller
public class UploadController {

    /** Taille maximale acceptee par fichier, cote metier. */
    private static final long TAILLE_MAX = 2L * 1024 * 1024; // 2 Mo

    /** Types MIME acceptes pour une piece d'identite. */
    private static final List<String> TYPES_ACCEPTES =
            List.of("image/jpeg", "image/png", "application/pdf");

    @UrlMapping(url = "/upload", method = "GET")
    public String formulaire(Model model) {
        model.setAttribute("titre", "Sprint 8 - Envoi de fichiers");
        return "upload/form";
    }

    /**
     * Meme signature que l'exemple du sprint, avec les deux MapFile.
     * Le binding d'objet du Sprint 7 bis continue de remplir Employe,
     * meme si le corps de la requete est du multipart.
     */
    @UrlMapping(url = "/upload/save", method = "POST")
    public String save(Model model, HttpServletRequest request, Employe employe,
                       MapFile rectoCIN, MapFile versoCIN) {

        // --- 1. On contrôle les donnees texte ---
        if (employe == null || estVide(employe.getNom())) {
            model.setAttribute("erreur", "Le champ « nom » est obligatoire.");
            return "upload/form";
        }

        // --- 2. Aucun fichier recu : les MapFile sont null (voir ParamBinder) ---
        if (rectoCIN == null && versoCIN == null) {
            model.setAttribute("erreur",
                    "Aucun fichier recu. Verifie que le formulaire contient bien "
                            + "enctype=\"multipart/form-data\" et que des fichiers ont ete choisis.");
            return "upload/form";
        }

        // --- 3. Validation puis enregistrement sur le serveur ---
        File dossier = dossierStockage(request);
        List<File> ecrits = new ArrayList<>();
        List<String> infos = new ArrayList<>();

        try {
            ecrits.addAll(traiter(rectoCIN, "rectoCIN", "recto-" + employe.getNom(), dossier, infos));
            ecrits.addAll(traiter(versoCIN, "versoCIN", "verso-" + employe.getNom(), dossier, infos));

        } catch (FileStorageException e) {
            // Message deja comprehensible : on le remonte tel quel a l'utilisateur
            model.setAttribute("erreur", e.getMessage());
            model.setAttribute("employe", employe);
            return "upload/form";
        }

        // --- 4. Affichage du resultat ---
        model.setAttribute("employe", employe);
        model.setAttribute("rectoCIN", rectoCIN);
        model.setAttribute("versoCIN", versoCIN);
        model.setAttribute("dossier", dossier.getAbsolutePath());
        model.setAttribute("fichiersEcrits", ecrits);
        model.setAttribute("infos", infos);
        return "upload/resultat";
    }

    /**
     * Reproduction EXACTE de la signature demandee par le sprint 8 :
     *
     *     public String save(Employe employe, MapFile rectoCIN, MapFile versoCIN)
     *
     * Aucun Model, aucun HttpServletRequest : le framework remplit l'Employe
     * et construit les deux MapFile a partir des noms de champs du formulaire.
     * Le nom de la vue retournee depend du resultat, ce qui permet de verifier
     * le binding sans attribut de modele.
     *
     * Les fichiers sont ecrits dans ${catalina.base}/upload-data, c'est-a-dire
     * HORS de l'application web : ils ne sont donc accessibles par aucune URL.
     */
    @UrlMapping(url = "/upload/simple", method = "POST")
    public String save(Employe employe, MapFile rectoCIN, MapFile versoCIN) {

        if (employe == null || estVide(employe.getNom())
                || rectoCIN == null || versoCIN == null) {
            return "upload/echec";
        }

        try {
            for (MapFile fichier : List.of(rectoCIN, versoCIN)) {
                FileStorage.verifierTaille(fichier, TAILLE_MAX);
                FileStorage.verifierType(fichier, TYPES_ACCEPTES);
            }

            File dossier = dossierHorsWebapp();
            FileStorage.sauvegarder(rectoCIN, dossier, "recto-" + employe.getNom());
            FileStorage.sauvegarder(versoCIN, dossier, "verso-" + employe.getNom());

        } catch (FileStorageException e) {
            return "upload/echec";
        }

        return "upload/success";
    }

    /**
     * Dossier de stockage de secours, utilise quand le controleur n'a pas
     * acces a la requete : ${catalina.base}/upload-data.
     */
    private static File dossierHorsWebapp() {
        String base = System.getProperty("catalina.base");
        if (base == null || base.isBlank()) {
            base = System.getProperty("java.io.tmpdir");
        }
        return new File(base, "upload-data");
    }

    /**
     * Valide et enregistre un fichier. Retourne la liste (0 ou 1 element)
     * des fichiers ecrits afin de garder le controleur lisible.
     */
    private List<File> traiter(MapFile fichier, String nomChamp, String prefixe,
                               File dossier, List<String> infos)
            throws FileStorageException {

        List<File> resultat = new ArrayList<>();

        if (fichier == null) {
            infos.add("Champ « " + nomChamp + " » : aucun fichier fourni (MapFile null).");
            return resultat;
        }

        // Securite : taille puis type, avant toute ecriture
        FileStorage.verifierTaille(fichier, TAILLE_MAX);
        FileStorage.verifierType(fichier, TYPES_ACCEPTES);

        File ecrit = FileStorage.sauvegarder(fichier, dossier, prefixe);
        resultat.add(ecrit);

        infos.add("Champ « " + nomChamp + " » : " + fichier.getFilename()
                + " (" + FileStorage.octetsEnKo(fichier.getSize())
                + ") enregistre sous " + ecrit.getName());

        return resultat;
    }

    /**
     * Dossier de stockage : /WEB-INF/upload de l'application.
     *
     * Place sous WEB-INF, il n'est PAS servi par le conteneur :
     * les fichiers deposes ne sont donc pas accessibles depuis un simple
     * navigateur, seul un controleur peut les lire et les renvoyer.
     *
     * Si le WAR n'est pas deploye en mode « exploded » (getRealPath null),
     * on retombe sur le dossier temporaire du servlet.
     */
    private File dossierStockage(HttpServletRequest request) {
        String chemin = request.getServletContext().getRealPath("/WEB-INF/upload");
        if (chemin != null) {
            return new File(chemin);
        }

        // WAR non deploye en mode "exploded" : getRealPath renvoie null.
        // On retombe sur le dossier temporaire du contexte.
        Object temp = request.getServletContext().getAttribute("jakarta.servlet.context.tempdir");
        File base = temp instanceof File ? (File) temp : new File(System.getProperty("java.io.tmpdir"));
        return new File(base, "upload");
    }

    private static boolean estVide(String valeur) {
        return valeur == null || valeur.isBlank();
    }
}
