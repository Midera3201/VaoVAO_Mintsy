package com.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marque une methode de controleur comme etant une Web API.
 *
 * Au lieu de renvoyer un nom de vue (String utilise par
 * viewPrefix + viewSuffix + forward), la methode renvoie un objet
 * Java qui sera serialise en JSON puis ecrit directement dans la
 * reponse HTTP.
 *
 * Exemple :
 *   @ApiJson
 *   @UrlMapping(url = "/api/employes", method = "GET")
 *   public List<Employe> liste() { return repo.findAll(); }
 *
 * Le @Retention(RUNTIME) est indispensable : sans lui l'annotation
 * n'est pas visible par la reflexion utilisee dans Utilitaire.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiJson {
}
