package com.framework.util;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Sprint 7 : binding automatique des parametres HTTP.
 *
 * Pour chaque parametre de la methode du controleur, ParamBinder decide :
 *
 *   1. Le type est fourni par le framework (Model, requete, reponse,
 *      session, contexte)   -> le framework injecte l'objet.
 *   2. Sinon                 -> le nom du parametre sert de cle :
 *                              request.getParameter(nom), puis conversion
 *                              vers le type Java attendu.
 *
 * Exemple :
 *   public String save(String nom, int age)
 *   POST nom=Rakoto&age=25
 *      -> nom  = "Rakoto"
 *      -> age  = 25
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

            // --- 2. Binding HTTP par nom de parametre ---
            String nom = parametres[i].getName();
            args[i] = lireValeur(request, nom, type);
        }

        return args;
    }

    /**
     * Lit le parametre HTTP et le convertit vers le type attendu.
     */
    private static Object lireValeur(HttpServletRequest request, String nom, Class<?> type) {

        // Tableau de chaînes : cas des cases a cocher multiples ou du multi-select
        if (type.isArray() && type.getComponentType() == String.class) {
            return request.getParameterValues(nom);
        }

        String valeur = request.getParameter(nom);
        return convertir(valeur, type, nom);
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
        } catch (NumberFormatException e) {
            // Valeur non convertible : on signale et on prend la valeur par defaut
            System.err.println("[Framework] Binding : le parametre '" + nom
                    + "' = \"" + valeur + "\" n'est pas convertible en " + type.getSimpleName()
                    + " -> valeur par defaut utilisee");
            return valeurParDefaut(type);
        }

        // Type non pris en charge par le binding simple (Sprint 7 bis : objets complexes)
        return valeurParDefaut(type);
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
