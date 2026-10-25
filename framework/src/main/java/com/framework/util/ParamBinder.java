package com.framework.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Binding automatique des arguments d'une methode de controleur.
 *
 * Sprint 7 : parametres simples.
 * Sprint 7 bis : objets Java.
 * Sprint 8 : fichiers (MapFile).
 * Sprint 9 : session utilisateur (Session).
 *
 * Pour chaque parametre de la methode, ParamBinder decide dans cet ordre :
 *
 *   1. Le type est fourni par le framework (Model, requete, reponse,
 *      HttpSession, Session, contexte) -> injection directe de l'objet.
 *   2. Tableau de String          -> request.getParameterValues(nom).
 *   3. Type simple (String, int, double, boolean, char, BigDecimal,
 *      enum...)                   -> request.getParameter(nom) + conversion.
 *   4. Type MapFile               -> request.getPart(nom) : le fichier est
 *      lu en binaire et enveloppe dans un MapFile (Sprint 8).
 *   5. Type objet instanciable    -> creation de l'objet, puis pour chacun
 *      de ses attributs, request.getParameter(nomAttribut) + conversion,
 *      affectation par setter si present, sinon par acces direct au champ.
 *
 * Exemples :
 *
 *   public String save(String nom, int age)          -> Sprint 7
 *   public String save(Employe employe)              -> Sprint 7 bis
 *       formulaire nom=Midera&prenom=Naivo
 *         -> employe.setNom("Midera")
 *         -> employe.setPrenom("Naivo")
 *   public String save(Employe employe, MapFile rectoCIN, MapFile versoCIN)
 *                                                      -> Sprint 8
 *       formulaire multipart avec deux <input type="file">
 *         -> employe rempli par les champs texte
 *         -> rectoCIN et versoCIN construits depuis request.getPart(...)
 *   public String profil(Session session)             -> Sprint 9
 *       -> session enveloppe la HttpSession de l'utilisateur
 *       -> session.getAttribute("nom") / setAttribute(...) / removeAttribute(...)
 *
 * Les parametres absents, vides ou non convertibles recoivent la valeur
 * par defaut du type (null, 0, 0.0, false) : aucune exception ne remonte.
 *
 * IMPORTANT : les noms de parametres sont lus par reflexion. Ils ne sont
 * disponibles que si le projet est compile avec l'option -parameters
 * (voir <parameters>true</parameters> dans le maven-compiler-plugin).
 */
public final class ParamBinder {

    private ParamBinder() {
    }

    /**
     * Construit le tableau d'arguments a passer a method.invoke().
     */
    public static Object[] bind(Method method, Model model,
                                HttpServletRequest request, HttpServletResponse response) {

        Parameter[] parametres = method.getParameters();
        Object[] args = new Object[parametres.length];

        avertirSiNomsAbsents(method, parametres);

        for (int i = 0; i < parametres.length; i++) {
            Class<?> type = parametres[i].getType();

            // --- 1. Objets fournis par le framework (injection par type) ---
            if (type == Model.class) {
                args[i] = model;
                continue;
            }
            if (type == HttpServletRequest.class) {
                args[i] = request;
                continue;
            }
            if (type == HttpServletResponse.class) {
                args[i] = response;
                continue;
            }
            if (type == HttpSession.class) {
                args[i] = request.getSession();
                continue;
            }
            // --- Sprint 9 : session du framework (ou sous-classe personnalisee) ---
            // HttpSession reste disponible pour les developpeurs qui ont
            // besoin de l'API Servlet brute, mais Session est l'interface
            // recommandee : elle conserve les donnees entre les requetes.
            if (Session.class.isAssignableFrom(type)) {
                args[i] = creerSession(type, request);
                continue;
            }
            if (type == ServletContext.class) {
                args[i] = request.getServletContext();
                continue;
            }

            String nom = parametres[i].getName();

            // --- 2. Tableau de chaines (checkbox multiples, multi-select) ---
            if (type.isArray() && type.getComponentType() == String.class) {
                args[i] = request.getParameterValues(nom);
                continue;
            }

            // --- 3. Type simple : binding par nom (Sprint 7) ---
            if (estTypeSimple(type)) {
                args[i] = convertir(request.getParameter(nom), type, nom);
                continue;
            }

            // --- 4. Champ fichier (Sprint 8) ---
            // AVANT estObjetLiable : MapFile possede un constructeur vide et
            // n'est pas une classe du JDK, il serait sinon pris a tort pour
            // un objet a remplir depuis des champs texte.
            if (type == MapFile.class) {
                args[i] = lireFichier(request, nom);
                continue;
            }

            // --- 5. Type objet : binding automatique (Sprint 7 bis) ---
            if (estObjetLiable(type)) {
                args[i] = lierObjet(request, type, nom);
                continue;
            }

            // Type non supporte (collection, interface, classe sans constructeur vide)
            args[i] = null;
        }

        return args;
    }

    /**
     * Sprint 9 : construit l'objet Session injecte dans le controleur.
     *
     * request.getSession() retourne la session existante de l'utilisateur
     * (identifiee par le cookie JSESSIONID) ou en cree une nouvelle. Les
     * donnees ecrites par le controleur restent donc disponibles lors des
     * requetes suivantes du meme navigateur.
     *
     * Si le parametre est une sous-classe de Session (SessionUtilisateur
     * par exemple), on l'instancie par reflexion : elle doit exposer un
     * constructeur public acceptant HttpSession.
     *
     * @return l'objet Session, ou null si la sous-classe ne peut pas etre creee
     */
    private static Object creerSession(Class<?> type, HttpServletRequest request) {

        HttpSession httpSession = request.getSession();

        if (type == Session.class) {
            return new Session(httpSession);
        }

        try {
            return type.getConstructor(HttpSession.class).newInstance(httpSession);

        } catch (NoSuchMethodException e) {
            signaler(request, "Session : la classe " + type.getName()
                    + " doit posseder un constructeur public (" + HttpSession.class.getSimpleName()
                    + "). Parametre laisse a null.");

        } catch (ReflectiveOperationException e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            signaler(request, "Session : impossible d'instancier " + type.getName()
                    + " (" + cause.getClass().getSimpleName()
                    + (cause.getMessage() != null ? " : " + cause.getMessage() : "")
                    + "). Parametre laisse a null.");
        }

        return null;
    }

    /**
     * Sprint 8 : construit un MapFile a partir du part multipart du meme
     * nom que le parametre.
     *
     * Retourne null (sans lever d'exception) et trace un message explicite
     * dans les logs si la requete n'est pas multipart, si le champ n'existe
     * pas, si le navigateur n'a rien choisi, ou si la lecture echoue.
     */
    private static MapFile lireFichier(HttpServletRequest request, String nom) {

        if (!estMultipart(request)) {
            signaler(request, "Upload : la requete n'est pas multipart/form-data, "
                    + "le parametre '" + nom + "' (MapFile) sera null. "
                    + "Ajoute enctype=\"multipart/form-data\" au formulaire.");
            return null;
        }

        Part part;
        try {
            part = request.getPart(nom);
        } catch (Exception e) {
            // couvre notamment IOException quand le fichier depasse maxFileSize
            signaler(request, "Upload : lecture du champ '" + nom
                    + "' impossible (" + e.getClass().getSimpleName()
                    + (e.getMessage() != null ? " : " + e.getMessage() : "") + ")");
            return null;
        }

        if (part == null) {
            signaler(request, "Upload : aucun champ nomme '" + nom + "' dans le formulaire.");
            return null;
        }

        String nomSoumis = part.getSubmittedFileName();

        // Le champ existe mais aucun fichier n'a ete selectionne
        if (nomSoumis == null || nomSoumis.isBlank()) {
            signaler(request, "Upload : aucun fichier selectionne pour le champ '" + nom + "'.");
            return null;
        }

        try (InputStream in = part.getInputStream()) {
            byte[] contenu = in.readAllBytes();

            if (contenu.length == 0) {
                signaler(request, "Upload : fichier vide pour le champ '"
                        + nom + "' (nom soumis : " + nomSoumis + ").");
            }

            return new MapFile(nomSoumis, part.getContentType(), contenu);

        } catch (IOException e) {
            signaler(request, "Upload : lecture du contenu du champ '" + nom
                    + "' impossible (" + e.getMessage() + ")");
            return null;
        }
    }

    /**
     * Trace un diagnostic du framework.
     *
     * On passe par ServletContext.log() et non System.err : sous Windows le
     * stderr de Tomcat part dans la console et n'est pas conserve. Avec log(),
     * le message se retrouve dans %CATALINA_HOME%\logs\localhost.<date>.log,
     * donc reellement consultable pendant les tests.
     */
    public static void signaler(HttpServletRequest request, String message) {
        String ligne = "[Framework] " + message;
        ServletContext contexte = request != null ? request.getServletContext() : null;
        if (contexte != null) {
            contexte.log(ligne);
        } else {
            System.err.println(ligne);
        }
    }

    /**
     * Une requete est multipart si son Content-Type commence par
     * multipart/form-data. Sans cette verification, request.getPart()
     * leve IllegalStateException.
     */
    public static boolean estMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase(Locale.ROOT).startsWith("multipart/form-data");
    }

    /**
     * Sprint 7 bis : cree une instance de la classe, puis remplit chacun
     * de ses attributs a partir des parametres de la requete.
     *
     * Les attributs absents de la requete sont laisses a leur valeur
     * par defaut (celle du constructeur).
     */
    private static Object lierObjet(HttpServletRequest request, Class<?> type, String nomParametre) {

        Object instance;
        try {
            instance = type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            signaler(request, "Binding objet : impossible d'instancier "
                    + type.getName() + " (" + e.getMessage() + ")");
            return null;
        }

        for (Field field : type.getDeclaredFields()) {

            // On ignore les champs qui ne representent pas une donnee de formulaire
            if (Modifier.isStatic(field.getModifiers())
                    || Modifier.isFinal(field.getModifiers())
                    || field.isSynthetic()) {
                continue;
            }

            // Un seul niveau de profondeur : les sous-objets ne sont pas lies ici
            if (!estTypeSimple(field.getType())) {
                continue;
            }

            String valeur = request.getParameter(field.getName());

            // Variante prefixee, pratique quand un formulaire contient
            // plusieurs objets : employe.nom, employe.prenom, ...
            if (valeur == null && nomParametre != null) {
                valeur = request.getParameter(nomParametre + "." + field.getName());
            }

            // Attribut absent : on ne touche pas a la valeur par defaut
            if (valeur == null) {
                continue;
            }

            Object valeurConvertie = convertir(valeur, field.getType(),
                    type.getSimpleName() + "." + field.getName());

            affecter(instance, field, valeurConvertie);
        }

        return instance;
    }

    /**
     * Affecte une valeur a un attribut.
     * Priorite au setter s'il existe (setNom, setAge...), sinon ecriture
     * directe dans le champ prive via la reflexion.
     */
    private static void affecter(Object instance, Field field, Object valeur) {

        String nomSetter = "set" + Character.toUpperCase(field.getName().charAt(0))
                + field.getName().substring(1);

        try {
            Method setter = instance.getClass().getMethod(nomSetter, field.getType());
            setter.invoke(instance, valeur);
            return;
        } catch (NoSuchMethodException e) {
            // Pas de setter : on ecrira directement dans le champ
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            System.err.println("[Framework] Binding objet : setter " + nomSetter
                    + " en erreur (" + e.getMessage() + ") -> ecriture directe du champ");
        }

        try {
            field.setAccessible(true);
            field.set(instance, valeur);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            System.err.println("[Framework] Binding objet : champ " + field.getName()
                    + " non accessible (" + e.getMessage() + ")");
        }
    }

    /**
     * Un type simple se convertit directement depuis une chaine.
     * Tout le reste est candidat au binding d'objet.
     */
    public static boolean estTypeSimple(Class<?> type) {
        return (type.isPrimitive() && type != void.class)
                || type == String.class
                || type == Boolean.class
                || type == Character.class
                || type == Byte.class
                || type == Short.class
                || type == Integer.class
                || type == Long.class
                || type == Float.class
                || type == Double.class
                || Number.class.isAssignableFrom(type)
                || type.isEnum();
    }

    /**
     * Un type peut etre lie comme objet s'il est instanciable et qu'il
     * represente une donnee metier : ni interface, ni classe abstraite,
     * ni collection, ni tableau, ni classe du JDK.
     */
    public static boolean estObjetLiable(Class<?> type) {
        if (type.isInterface() || type.isArray()) {
            return false;
        }
        // Fichiers geres par lireFichier(), pas par le binding d'objets
        if (type == MapFile.class) {
            return false;
        }
        if (Modifier.isAbstract(type.getModifiers())) {
            return false;
        }
        if (Collection.class.isAssignableFrom(type) || Map.class.isAssignableFrom(type)) {
            return false;
        }
        if (type.getName().startsWith("java.")) {
            return false;
        }
        try {
            type.getDeclaredConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    /**
     * Conversion d'une valeur texte vers le type Java cible.
     * Ne leve jamais d'exception : en cas de probleme, la valeur par
     * defaut du type est retournee.
     */
    public static Object convertir(String valeur, Class<?> type, String nom) {

        // String : on garde la valeur telle quelle, null reste null
        if (type == String.class) {
            return valeur;
        }

        // Parametre absent (null) ou vide -> valeur par defaut, sans bruit
        if (valeur == null || valeur.trim().isEmpty()) {
            return valeurParDefaut(type);
        }

        String v = valeur.trim();
        try {
            if (type == int.class || type == Integer.class) {
                return Integer.valueOf(v);
            }
            if (type == long.class || type == Long.class) {
                return Long.valueOf(v);
            }
            if (type == double.class || type == Double.class) {
                return Double.valueOf(v);
            }
            if (type == float.class || type == Float.class) {
                return Float.valueOf(v);
            }
            if (type == short.class || type == Short.class) {
                return Short.valueOf(v);
            }
            if (type == byte.class || type == Byte.class) {
                return Byte.valueOf(v);
            }
            if (type == boolean.class || type == Boolean.class) {
                return versBooleen(v);
            }
            if (type == char.class || type == Character.class) {
                return v.charAt(0);
            }
            if (type == BigDecimal.class) {
                return new BigDecimal(v);
            }
            if (type == BigInteger.class) {
                return new BigInteger(v);
            }
            if (type.isEnum()) {
                return versEnum(v, type);
            }
        } catch (NumberFormatException e) {
            // Valeur non convertible : on signale et on prend la valeur par defaut
            System.err.println("[Framework] Binding : le parametre '" + nom
                    + "' = \"" + valeur + "\" n'est pas convertible en " + type.getSimpleName()
                    + " -> valeur par defaut utilisee");
            return valeurParDefaut(type);
        }

        // Type non pris en charge par le binding
        return valeurParDefaut(type);
    }

    /**
     * Recherche la constante d'enum dont le nom correspond a la valeur.
     * Comparaison insensible a la casse.
     */
    private static Object versEnum(String valeur, Class<?> type) {
        for (Object constante : type.getEnumConstants()) {
            if (((Enum<?>) constante).name().equalsIgnoreCase(valeur)) {
                return constante;
            }
        }
        System.err.println("[Framework] Binding : \"" + valeur + "\" ne correspond a aucune constante de "
                + type.getSimpleName() + " -> null");
        return null;
    }

    /**
     * Interpretation d'une valeur texte comme booleen.
     * Accepte : true/false, on/off, yes/no, oui/non, 1/0.
     */
    private static boolean versBooleen(String v) {
        return v.equalsIgnoreCase("true")
                || v.equalsIgnoreCase("on")
                || v.equalsIgnoreCase("yes")
                || v.equalsIgnoreCase("oui")
                || v.equals("1");
    }

    /**
     * Valeur par defaut d'un type quand la conversion est impossible.
     * null pour les types objets, valeur nulle pour les primitifs.
     */
    private static Object valeurParDefaut(Class<?> type) {
        if (!type.isPrimitive()) {
            return null;
        }
        if (type == boolean.class) return Boolean.FALSE;
        if (type == char.class) return (char) 0;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        return null;
    }

    /**
     * Avertit une seule fois si la compilation n'a pas conserve les noms
     * de parametres : dans ce cas ils valent arg0, arg1, ... et le binding
     * ne peut pas retrouver les champs du formulaire.
     */
    private static void avertirSiNomsAbsents(Method method, Parameter[] parametres) {
        List<Parameter> sansNom = Arrays.stream(parametres)
                .filter(p -> !p.isNamePresent())
                .filter(p -> p.getType() != Model.class)
                .filter(p -> p.getType() != HttpServletRequest.class)
                .filter(p -> p.getType() != HttpServletResponse.class)
                .filter(p -> p.getType() != HttpSession.class)
                .filter(p -> p.getType() != ServletContext.class)
                .filter(p -> !Session.class.isAssignableFrom(p.getType()))
                .toList();

        if (!sansNom.isEmpty()) {
            System.err.println("[Framework] ATTENTION : les noms de parametres sont absents pour "
                    + method.getName() + "(...). Ajoute <parameters>true</parameters> dans le "
                    + "maven-compiler-plugin, sinon le binding HTTP ne fonctionnera pas.");
        }
    }
}
