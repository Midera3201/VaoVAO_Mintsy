package com.framework.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
import java.util.Map;

/**
 * Binding automatique des arguments d'une methode de controleur.
 *
 * Sprint 7 : parametres simples.
 * Sprint 7 bis : objets Java.
 *
 * Pour chaque parametre de la methode, ParamBinder decide dans cet ordre :
 *
 *   1. Le type est fourni par le framework (Model, requete, reponse,
 *      session, contexte)        -> injection directe de l'objet.
 *   2. Tableau de String          -> request.getParameterValues(nom).
 *   3. Type simple (String, int, double, boolean, char, BigDecimal,
 *      enum...)                   -> request.getParameter(nom) + conversion.
 *   4. Type objet instanciable    -> creation de l'objet, puis pour chacun
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

            // --- 4. Type objet : binding automatique (Sprint 7 bis) ---
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
            System.err.println("[Framework] Binding objet : impossible d'instancier "
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
                .toList();

        if (!sansNom.isEmpty()) {
            System.err.println("[Framework] ATTENTION : les noms de parametres sont absents pour "
                    + method.getName() + "(...). Ajoute <parameters>true</parameters> dans le "
                    + "maven-compiler-plugin, sinon le binding HTTP ne fonctionnera pas.");
        }
    }
}
