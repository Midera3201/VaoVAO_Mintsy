package com.framework.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Marque une classe comme controleur du framework.
 *
 * Sprint 11 : le framework instancie une seule fois chaque classe annotee
 * @controller au demarrage. La meme instance est ensuite reutilisee pour
 * toutes les methodes et toutes les requetes de ce controleur.
 *
 * Un controleur doit donc rester sans etat mutable (ou etre thread-safe),
 * car il est partage par les requetes concurrentes.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface controller {
}
