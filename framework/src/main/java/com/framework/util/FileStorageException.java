package com.framework.util;

/**
 * Sprint 8 : levee quand un fichier recu ne peut pas etre valide ou
 * enregistre (taille, type, nom, ecriture...).
 *
 * Le message est concu pour etre affiche tel quel a l'utilisateur ou
 * dans le Model, afin d'eviter les erreurs silencieuses.
 */
public class FileStorageException extends Exception {

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
