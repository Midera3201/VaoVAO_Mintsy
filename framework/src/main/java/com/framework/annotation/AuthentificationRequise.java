package com.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 10 : protege une methode d'action de controleur.
 *
 * Le framework verifie, AVANT l'invocation de la methode, qu'un utilisateur
 * est authentifie. Si ce n'est pas le cas, la methode n'est pas executee :
 *
 *   - requete JSON  -> reponse 401 avec un message JSON ;
 *   - requete HTML  -> redirection vers auth.loginUrl si configure,
 *                      sinon erreur HTTP 401.
 *
 * Par defaut, le framework lit l'attribut de session dont le nom est configure
 * dans web.xml :
 *
 *   <context-param>
 *       <param-name>auth.sessionAttribute</param-name>
 *       <param-value>utilisateur</param-value>
 *   </context-param>
 *
 * L'attribut est considere comme authentifie s'il existe et que sa valeur
 * n'est ni null, ni false, ni une chaine vide.
 *
 * Exemple :
 *
 *   @AuthentificationRequise
 *   @UrlMapping(url = "/admin", method = "GET")
 *   public String admin(Model model, Session session) {
 *       return "admin/dashboard";
 *   }
 *
 * Pour une methode isolee, on peut remplacer le nom configure :
 *
 *   @AuthentificationRequise(attribut = "membre")
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AuthentificationRequise {

    /**
     * Nom de l'attribut de session a controler.
     * Vide : on utilise la configuration globale auth.sessionAttribute.
     */
    String attribut() default "";
}
