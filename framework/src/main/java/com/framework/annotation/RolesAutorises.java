package com.framework.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sprint 10 bis : restreint une methode aux utilisateurs possedant
 * l'un des roles autorises.
 *
 * Cette annotation implique aussi la verification de l'authentification :
 * inutile d'ajouter @AuthentificationRequise, mais les deux annotations
 * peuvent coexister.
 *
 * Un seul role :
 *
 *   @RolesAutorises({"ADMIN"})
 *   @UrlMapping(url = "/admin/utilisateurs", method = "GET")
 *   public String utilisateurs(Model model, Session session) { ... }
 *
 * Plusieurs roles autorises :
 *
 *   @RolesAutorises({"ADMIN", "MANAGER"})
 *
 * La comparaison est insensible a la casse : "admin", "Admin" et "ADMIN"
 * designent le meme role.
 *
 * Le role de l'utilisateur est lu dans l'attribut de session configure
 * dans web.xml :
 *
 *   <context-param>
 *       <param-name>auth.roleAttribute</param-name>
 *       <param-value>role</param-value>
 *   </context-param>
 *
 * Cet attribut peut contenir :
 *   - une chaine : "ADMIN" ou "ADMIN,MANAGER" ;
 *   - une collection ou un tableau de roles ;
 *   - une constante d'enum.
 *
 * Si la methode est appelee sans authentification, le framework renvoie 401
 * (ou redirige vers la page de connexion). Si l'utilisateur est authentifie
 * mais ne possede aucun role autorise, le framework renvoie 403.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RolesAutorises {

    /**
     * Liste des roles autorises. Une seule valeur suffit pour un role unique.
     */
    String[] value();
}
