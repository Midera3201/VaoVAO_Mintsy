package com.framework.util;

import jakarta.servlet.http.HttpSession;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sprint 9 : session utilisateur du framework.
 *
 * Cette classe est une enveloppe (wrapper) autour de {@link HttpSession}.
 * Un controleur n'a donc plus besoin de manipuler l'API Servlet :
 *
 *   public String profil(Session session) {
 *       String nom = session.getString("nom");
 *       session.setAttribute("nom", "Midera");
 *       session.removeAttribute("temporaire");
 *       return "profil";
 *   }
 *
 * Le framework injecte automatiquement l'objet Session dans les methodes
 * de controleur qui declarent un parametre de ce type (voir ParamBinder).
 *
 * Les donnees sont stockees dans la HttpSession sous-jacente : elles sont
 * donc conservees d'une requete HTTP a l'autre pour un meme utilisateur,
 * jusqu'a expiration de la session ou appel de {@link #invalidate()}.
 *
 * Sous-classe personnalisee :
 *   Une application peut etendre cette classe (par exemple SessionUtilisateur)
 *   a condition de fournir un constructeur public acceptant HttpSession.
 *   ParamBinder instanciera alors sa classe et l'injectera dans le controleur.
 *
 *   public class SessionUtilisateur extends Session {
 *       public SessionUtilisateur(HttpSession httpSession) {
 *           super(httpSession);
 *       }
 *   }
 */
public class Session {

    /** Nom d'attribut par defaut de l'utilisateur connecte. */
    public static final String ATTRIBUT_UTILISATEUR = "utilisateur";

    /** Nom d'attribut par defaut du role de l'utilisateur connecte. */
    public static final String ATTRIBUT_ROLE = "role";

    /** Valeur conventionnelle du role administrateur. */
    public static final String ROLE_ADMIN = "admin";

    private final HttpSession httpSession;

    /**
     * @param httpSession session fournie par le conteneur (jamais null)
     */
    public Session(HttpSession httpSession) {
        if (httpSession == null) {
            throw new IllegalArgumentException("La HttpSession ne peut pas etre null");
        }
        this.httpSession = httpSession;
    }

    // ------------------------------------------------------------------
    // Identite et cycle de vie
    // ------------------------------------------------------------------

    /**
     * Identifiant de session attribue par le conteneur (valeur du cookie
     * JSESSIONID pour le client).
     */
    public String getId() {
        return httpSession.getId();
    }

    /** true si la session vient d'etre creee pour cette requete. */
    public boolean estNouvelle() {
        return httpSession.isNew();
    }

    /** Date de creation de la session, en millisecondes depuis epoch. */
    public long getDateCreation() {
        return httpSession.getCreationTime();
    }

    /** Date du dernier acces a la session, en millisecondes depuis epoch. */
    public long getDateDernierAcces() {
        return httpSession.getLastAccessedTime();
    }

    /**
     * Duree d'inactivite maximale en secondes. Une valeur negative signifie
     * que la session n'expire jamais.
     */
    public int getDureeInactivite() {
        return httpSession.getMaxInactiveInterval();
    }

    /** Modifie la duree d'inactivite maximale, en secondes. */
    public void setDureeInactivite(int secondes) {
        httpSession.setMaxInactiveInterval(secondes);
    }

    /**
     * Detruit la session et toutes ses donnees. Le conteneur cree une
     * nouvelle session a la requete suivante.
     */
    public void invalidate() {
        httpSession.invalidate();
    }

    /** Alias francais de {@link #invalidate()}. */
    public void detruire() {
        invalidate();
    }

    /** Acces a la HttpSession brute, pour les cas particuliers. */
    public HttpSession getHttpSession() {
        return httpSession;
    }

    // ------------------------------------------------------------------
    // Lecture / ecriture des donnees
    // ------------------------------------------------------------------

    /**
     * Ajoute ou remplace une donnee de session.
     * Une valeur null supprime l'attribut (comportement de HttpSession).
     */
    public void setAttribute(String nom, Object valeur) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de l'attribut de session est obligatoire");
        }
        httpSession.setAttribute(nom, valeur);
    }

    /** Alias francais de {@link #setAttribute(String, Object)}. */
    public void setAttribut(String nom, Object valeur) {
        setAttribute(nom, valeur);
    }

    /**
     * Retourne la donnee associee au nom, ou null si elle n'existe pas.
     */
    public Object getAttribute(String nom) {
        return nom == null ? null : httpSession.getAttribute(nom);
    }

    /** Alias francais de {@link #getAttribute(String)}. */
    public Object getAttribut(String nom) {
        return getAttribute(nom);
    }

    /**
     * Version typee : retourne null si la donnee est absente ou si elle
     * n'est pas du type demande (aucune ClassCastException).
     */
    public <T> T getAttribute(String nom, Class<T> type) {
        Object valeur = getAttribute(nom);
        if (valeur == null || type == null || !type.isInstance(valeur)) {
            return null;
        }
        return type.cast(valeur);
    }

    /** Lit une chaine ; les autres types sont convertis avec String.valueOf. */
    public String getString(String nom) {
        Object valeur = getAttribute(nom);
        return valeur == null ? null : String.valueOf(valeur);
    }

    /** Lit un entier ; retourne la valeur par defaut si absent/non convertible. */
    public int getInt(String nom, int valeurParDefaut) {
        Object valeur = getAttribute(nom);
        if (valeur instanceof Number) {
            return ((Number) valeur).intValue();
        }
        if (valeur != null) {
            try {
                return Integer.parseInt(String.valueOf(valeur).trim());
            } catch (NumberFormatException e) {
                return valeurParDefaut;
            }
        }
        return valeurParDefaut;
    }

    /** Lit un booleen ; retourne la valeur par defaut si absent. */
    public boolean getBoolean(String nom, boolean valeurParDefaut) {
        Object valeur = getAttribute(nom);
        if (valeur instanceof Boolean) {
            return (Boolean) valeur;
        }
        if (valeur != null) {
            String texte = String.valueOf(valeur).trim();
            return texte.equalsIgnoreCase("true")
                    || texte.equalsIgnoreCase("on")
                    || texte.equalsIgnoreCase("oui")
                    || texte.equals("1");
        }
        return valeurParDefaut;
    }

    /** true si la donnee existe dans la session. */
    public boolean hasAttribute(String nom) {
        return getAttribute(nom) != null;
    }

    /** Alias francais de {@link #hasAttribute(String)}. */
    public boolean contient(String nom) {
        return hasAttribute(nom);
    }

    /**
     * Supprime une donnee et retourne l'ancienne valeur (ou null).
     */
    public Object removeAttribute(String nom) {
        if (nom == null) {
            return null;
        }
        Object ancienne = httpSession.getAttribute(nom);
        httpSession.removeAttribute(nom);
        return ancienne;
    }

    /** Alias francais de {@link #removeAttribute(String)}. */
    public Object supprimerAttribut(String nom) {
        return removeAttribute(nom);
    }

    /** Supprime plusieurs donnees d'un seul appel. */
    public void removeAttributes(String... noms) {
        if (noms == null) {
            return;
        }
        for (String nom : noms) {
            removeAttribute(nom);
        }
    }

    /** Supprime toutes les donnees de la session (la session reste active). */
    public void clear() {
        // Copie des noms avant suppression : on ne modifie pas la collection
        // pendant son parcours (toutes les implémentations ne sont pas concurrentes).
        List<String> noms = Collections.list(httpSession.getAttributeNames());
        for (String nom : noms) {
            httpSession.removeAttribute(nom);
        }
    }

    /** Alias francais de {@link #clear()}. */
    public void vider() {
        clear();
    }

    /**
     * Copie de toutes les donnees de session, dans l'ordre d'insertion.
     * La map retournee est un instantane : la modifier ne change pas la session.
     */
    public Map<String, Object> getAttributes() {
        Map<String, Object> donnees = new LinkedHashMap<>();
        Enumeration<String> noms = httpSession.getAttributeNames();
        while (noms.hasMoreElements()) {
            String nom = noms.nextElement();
            donnees.put(nom, httpSession.getAttribute(nom));
        }
        return Collections.unmodifiableMap(donnees);
    }

    /** Alias francais de {@link #getAttributes()}. */
    public Map<String, Object> getDonnees() {
        return getAttributes();
    }

    // ------------------------------------------------------------------
    // Utilisateur connecte et role (raccourcis)
    // ------------------------------------------------------------------

    /** Enregistre l'utilisateur connecte et son role. */
    public void connecter(String utilisateur, String role) {
        setAttribute(ATTRIBUT_UTILISATEUR, utilisateur);
        if (role == null || role.isBlank()) {
            removeAttribute(ATTRIBUT_ROLE);
        } else {
            setAttribute(ATTRIBUT_ROLE, role);
        }
    }

    /** Enregistre l'utilisateur connecte sans role particulier. */
    public void connecter(String utilisateur) {
        connecter(utilisateur, null);
    }

    /** Supprime l'utilisateur et son role (les autres donnees restent). */
    public void deconnecter() {
        removeAttributes(ATTRIBUT_UTILISATEUR, ATTRIBUT_ROLE);
    }

    /** Nom de l'utilisateur connecte, ou null. */
    public String getUtilisateur() {
        return getString(ATTRIBUT_UTILISATEUR);
    }

    public void setUtilisateur(String utilisateur) {
        setAttribute(ATTRIBUT_UTILISATEUR, utilisateur);
    }

    /** Role de l'utilisateur connecte, ou null. */
    public String getRole() {
        return getString(ATTRIBUT_ROLE);
    }

    public void setRole(String role) {
        setAttribute(ATTRIBUT_ROLE, role);
    }

    /** true si un utilisateur est enregistre dans la session. */
    public boolean estConnecte() {
        String utilisateur = getUtilisateur();
        return utilisateur != null && !utilisateur.isBlank();
    }

    /** true si l'utilisateur connecte possede le role admin. */
    public boolean estAdmin() {
        return ROLE_ADMIN.equalsIgnoreCase(getRole());
    }
}
