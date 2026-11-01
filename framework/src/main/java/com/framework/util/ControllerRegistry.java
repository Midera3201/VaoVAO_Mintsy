package com.framework.util;

import jakarta.servlet.ServletContext;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sprint 11 : registre des instances de controleurs.
 *
 * Le framework ne cree plus une nouvelle instance a chaque requete. Chaque
 * classe annotee @controller est instanciee UNE SEULE FOIS au demarrage, puis
 * la meme instance est reutilisee pour toutes les methodes et toutes les
 * requetes de cette classe.
 *
 * Le registre est stocke dans le ServletContext :
 *
 *   clé   : Class<?> du controleur
 *   valeur : instance unique du controleur
 *
 * IMPORTANT : un controleur est desormais partage par toutes les requetes
 * concurrentes. Il doit donc rester sans etat mutable, ou proteger ses
 * traitements avec une synchronisation adaptee. Les donnees propres a une
 * requete doivent passer par les parametres (Model, Session, request...) et
 * non par des champs d'instance.
 */
public final class ControllerRegistry {

    /** Nom de l'attribut du ServletContext contenant le registre. */
    public static final String CONTEXT_ATTRIBUT = "controllerInstances";

    private ControllerRegistry() {
    }

    /**
     * Instancie une seule fois chaque classe de controleur fournie.
     * Les doublons sont automatiquement supprimes.
     */
    public static Map<Class<?>, Object> creerInstances(Collection<Class<?>> classes) {
        Map<Class<?>, Object> instances = new ConcurrentHashMap<>();

        for (Class<?> classe : new LinkedHashSet<>(classes)) {
            Object instance = instancier(classe);
            instances.put(classe, instance);
            System.out.println("[Framework] Controleur singleton : "
                    + classe.getName() + " -> " + instance);
        }

        return instances;
    }

    /**
     * Enregistre le registre dans le ServletContext pour que le
     * FrontControllerServlet puisse le retrouver a chaque requete.
     */
    public static void enregistrer(ServletContext context,
                                   Map<Class<?>, Object> instances) {
        context.setAttribute(CONTEXT_ATTRIBUT, instances);
    }

    /**
     * Retourne l'instance unique du controleur.
     *
     * En pratique, le registre est deja rempli par ApplicationListener au
     * demarrage. Le calcul a la demande reste disponible comme filet de
     * securite (par exemple pour un controleur ajoute dynamiquement).
     */
    public static Object getInstance(ServletContext context, Class<?> classe) {
        Map<Class<?>, Object> instances = recupererRegistre(context);

        Object instance = instances.get(classe);
        if (instance != null) {
            return instance;
        }

        return instances.computeIfAbsent(classe, ControllerRegistry::instancier);
    }

    /**
     * Cree le controleur une seule fois, meme si deux requetes arrivent en
     * parallele avant son enregistrement.
     */
    private static Object instancier(Class<?> classe) {
        try {
            return classe.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof InvocationTargetException
                    && e.getCause() != null ? e.getCause() : e;

            throw new IllegalStateException(
                    "Impossible d'instancier le controleur " + classe.getName()
                            + " : un constructeur public sans argument est requis",
                    cause);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Class<?>, Object> recupererRegistre(ServletContext context) {
        Object attribut = context.getAttribute(CONTEXT_ATTRIBUT);

        if (attribut instanceof Map<?, ?>) {
            return (Map<Class<?>, Object>) attribut;
        }

        // Filet de securite : le listener n'a pas encore cree le registre.
        synchronized (context) {
            attribut = context.getAttribute(CONTEXT_ATTRIBUT);
            if (attribut instanceof Map<?, ?>) {
                return (Map<Class<?>, Object>) attribut;
            }

            Map<Class<?>, Object> instances = new ConcurrentHashMap<>();
            context.setAttribute(CONTEXT_ATTRIBUT, instances);
            return instances;
        }
    }
}
