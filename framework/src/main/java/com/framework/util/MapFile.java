package com.framework.util;

import java.util.Arrays;

/**
 * Sprint 8 : representation d'un fichier recu depuis un formulaire
 * multipart/form-data.
 *
 * Le framework construit une instance de MapFile pour chaque champ
 * <input type="file"> et la transmet au parametre de meme type et de meme
 * nom que le champ HTML :
 *
 *   HTML  : <input type="file" name="rectoCIN">
 *   Java  : public String save(MapFile rectoCIN)
 *
 * Contenu :
 *   filename    : nom original du fichier tel qu'envoye par le navigateur
 *                 (a ne JAMAIS utiliser directement pour ecrire sur disque,
 *                  voir FileStorage.nomValide)
 *   contentType : type MIME (image/png, application/pdf...)
 *   bytes       : contenu binaire complet du fichier
 */
public class MapFile {

    private String filename;
    private String contentType;
    private byte[] bytes;

    public MapFile() {
    }

    public MapFile(String filename, String contentType, byte[] bytes) {
        this.filename = filename;
        this.contentType = contentType;
        this.bytes = bytes;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    /**
     * @return true si le fichier n'a pas de contenu (aucun fichier envoye,
     *         champ fichier vide, ou echec de lecture).
     */
    public boolean isEmpty() {
        return bytes == null || bytes.length == 0;
    }

    /**
     * @return taille en octets, 0 si aucun contenu.
     */
    public long getSize() {
        return bytes == null ? 0L : bytes.length;
    }

    @Override
    public String toString() {
        return "MapFile[filename=" + filename
                + ", contentType=" + contentType
                + ", taille=" + getSize() + " octets"
                + (isEmpty() ? ", VIDE" : "")
                + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapFile that = (MapFile) o;
        return java.util.Objects.equals(filename, that.filename)
                && java.util.Objects.equals(contentType, that.contentType)
                && Arrays.equals(bytes, that.bytes);
    }

    @Override
    public int hashCode() {
        int result = java.util.Objects.hash(filename, contentType);
        result = 31 * result + Arrays.hashCode(bytes);
        return result;
    }
}
