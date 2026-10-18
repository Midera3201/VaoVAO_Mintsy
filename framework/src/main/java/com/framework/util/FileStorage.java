package com.framework.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Locale;

/**
 * Sprint 8 : validation et enregistrement d'un fichier recu (MapFile)
 * sur le disque du serveur.
 *
 * La recuperation du fichier et son stockage sont deux operations
 * distinctes : le framework se contente de construire l'objet MapFile,
 * c'est le controleur qui decide de le sauvegarder.
 *
 * Securite appliquee :
 *   - le nom envoye par le navigateur n'est jamais utilise tel quel ;
 *   - tout chemin (C:\Users\..\, /etc/, ../../) est supprime ;
 *   - seuls les caracteres [a-zA-Z0-9._-] sont conserves ;
 *   - un controle canonique du chemin empeche toute traversee de
 *     repertoire, meme si le nom avait echappe au filtre ;
 *   - la taille et le type MIME peuvent etre verifies avant ecriture.
 *
 * Le dossier de stockage est passe par le controleur : dans ce projet,
 * c'est /WEB-INF/upload de l'application, donc non accessible depuis
 * l'exterieur (voir UploadController.dossierStockage).
 */
public final class FileStorage {

    private FileStorage() {
    }

    /**
     * Verifie la taille maximale d'un fichier.
     *
     * @param tailleMaximaleOctets taille maximale acceptee, en octets
     * @throws FileStorageException si le fichier est vide ou trop gros
     */
    public static void verifierTaille(MapFile fichier, long tailleMaximaleOctets) throws FileStorageException {
        if (fichier == null) {
            throw new FileStorageException("Aucun fichier recu (le champ est absent ou null).");
        }
        if (fichier.isEmpty()) {
            throw new FileStorageException("Le fichier \""
                    + valeur(fichier.getFilename(), "(nom absent)")
                    + "\" est vide.");
        }
        if (fichier.getSize() > tailleMaximaleOctets) {
            throw new FileStorageException("Le fichier \""
                    + valeur(fichier.getFilename(), "(nom absent)")
                    + "\" fait " + octetsEnKo(fichier.getSize()) + ", la taille maximale est de "
                    + octetsEnKo(tailleMaximaleOctets) + ".");
        }
    }

    /**
     * Verifie que le type MIME du fichier appartient a la liste autorisee.
     * Comparaison insensible a la casse ; le sous-type joker (image/*)
     * est accepte.
     *
     * @param typesAcceptes par exemple "image/jpeg", "application/pdf", "image/*"
     * @throws FileStorageException si le type n'est pas autorise
     */
    public static void verifierType(MapFile fichier, Collection<String> typesAcceptes) throws FileStorageException {
        if (fichier == null) {
            throw new FileStorageException("Aucun fichier recu (le champ est absent ou null).");
        }
        if (typesAcceptes == null || typesAcceptes.isEmpty()) {
            return;
        }

        String type = fichier.getContentType();
        if (type == null || type.isBlank()) {
            throw new FileStorageException("Le fichier \""
                    + valeur(fichier.getFilename(), "(nom absent)")
                    + "\" n'a pas de type MIME, type refuse.");
        }

        String typeFichier = type.trim().toLowerCase(Locale.ROOT);
        for (String accepte : typesAcceptes) {
            if (accepte == null) {
                continue;
            }
            String attendu = accepte.trim().toLowerCase(Locale.ROOT);
            if (typeFichier.equals(attendu) || typeFichier.matches(attendu.replace("*", ".*"))) {
                return;
            }
        }

        throw new FileStorageException("Le type \"" + type + "\" du fichier \""
                + valeur(fichier.getFilename(), "(nom absent)")
                + "\" n'est pas autorise. Types acceptes : " + typesAcceptes + ".");
    }

    /**
     * Enregistre le contenu du fichier dans le dossier donne.
     *
     * Le fichier est ecrit sous un nom securise (voir nomValide) et, si le
     * fichier existe deja, un horodatage est ajoute pour eviter tout
     * ecrasement silencieux.
     *
     * @param dossier  dossier de destination (cree s'il n'existe pas)
     * @param prefixe  optionnel, ajoute devant le nom, ex. "recto"
     * @return le fichier effectivement cree sur le disque
     * @throws FileStorageException si le fichier est vide, le nom refuse,
     *         le dossier inaccessible ou l'ecriture echoue
     */
    public static File sauvegarder(MapFile fichier, File dossier, String prefixe) throws FileStorageException {
        if (fichier == null) {
            throw new FileStorageException("Aucun fichier a enregistrer (null).");
        }
        if (fichier.isEmpty()) {
            throw new FileStorageException("Le fichier \""
                    + valeur(fichier.getFilename(), "(nom absent)")
                    + "\" est vide, rien n'a ete enregistre.");
        }
        if (dossier == null) {
            throw new FileStorageException("Aucun dossier de destination fourni.");
        }

        if (!dossier.exists() && !dossier.mkdirs()) {
            throw new FileStorageException("Impossible de creer le dossier " + dossier.getAbsolutePath());
        }
        if (!dossier.isDirectory() || !dossier.canWrite()) {
            throw new FileStorageException("Le dossier " + dossier.getAbsolutePath()
                    + " n'existe pas ou n'est pas inscriptible.");
        }

        String nom = nomValide(fichier.getFilename());
        if (prefixe != null && !prefixe.isBlank()) {
            nom = nomValide(prefixe) + "-" + nom;
        }

        // Un nom deja pris recoit un horodatage : pas d'ecrasement
        File cible = new File(dossier, nom);
        if (cible.exists()) {
            cible = new File(dossier, System.currentTimeMillis() + "-" + nom);
        }

        // Controle canonique : derniere barriere contre la traversee de repertoire
        try {
            String dossierCanonique = dossier.getCanonicalPath();
            String cibleCanonique = cible.getCanonicalPath();
            if (!cibleCanonique.startsWith(dossierCanonique + File.separator)) {
                throw new FileStorageException("Nom de fichier refuse pour raisons de securite : \""
                        + fichier.getFilename() + "\".");
            }
        } catch (IOException e) {
            throw new FileStorageException("Impossible de verifier le chemin de destination : "
                    + e.getMessage(), e);
        }

        try {
            Files.write(cible.toPath(), fichier.getBytes());
        } catch (IOException e) {
            throw new FileStorageException("Ecriture impossible pour \""
                    + valeur(fichier.getFilename(), "(nom absent)") + "\" : " + e.getMessage(), e);
        }

        return cible;
    }

    /**
     * Fabrique un nom de fichier sur et n'accepte que les caracteres sûrs.
     *
     * Traitements, dans l'ordre :
     *   1. retrait de tout chemin : "C:\Users\a.png", "../../etc/passwd" -> "a.png", "passwd"
     *   2. suppression des doubles points restants
     *   3. remplacement de tout caractere hors [a-zA-Z0-9._-] par "_"
     *   4. retrait des points en debut/fin (empêche .htaccess)
     *   5. si le resultat est vide, un nom horodate est genere
     *
     * @param nomOriginal nom envoye par le navigateur (NON fiable)
     * @return un nom de fichier sûr, jamais vide
     */
    public static String nomValide(String nomOriginal) {
        if (nomOriginal == null || nomOriginal.isBlank()) {
            return "fichier-" + System.currentTimeMillis();
        }

        // 1. Retire tout chemin, quel que soit le separateur
        String nom = nomOriginal.replace('\\', '/');
        int dernierSeparateur = nom.lastIndexOf('/');
        if (dernierSeparateur >= 0) {
            nom = nom.substring(dernierSeparateur + 1);
        }

        // 2. Plus de double point
        nom = nom.replace("..", "");

        // 3. Caracteres autorises uniquement
        nom = nom.replaceAll("[^a-zA-Z0-9._-]", "_");
        nom = nom.replaceAll("\\.{2,}", ".");
        nom = nom.replaceAll("_+", "_");

        // 4. Ni point en debut ni en fin
        nom = nom.replaceAll("^\\.+|\\.+$", "");

        if (nom.isBlank()) {
            return "fichier-" + System.currentTimeMillis();
        }
        return nom;
    }

    /**
     * Formate une taille en octets en une chaine lisible.
     */
    public static String octetsEnKo(long octets) {
        if (octets < 1024) {
            return octets + " o";
        }
        if (octets < 1024 * 1024) {
            return String.format(Locale.ROOT, "%.1f Ko", octets / 1024.0);
        }
        return String.format(Locale.ROOT, "%.1f Mo", octets / (1024.0 * 1024.0));
    }

    private static String valeur(String v, String alternative) {
        return (v == null || v.isBlank()) ? alternative : v;
    }
}
