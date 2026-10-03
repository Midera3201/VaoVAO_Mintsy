package com.framework.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

/**
 * Conversion d'objets Java en JSON.
 *
 * Evite d'ajouter Jackson ou Gson comme dependance : le framework
 * reste autonome. Les types suivants sont geres :
 *
 *   null                      -> null
 *   String, Character, Enum   -> "texte"
 *   Boolean, Number           -> valeur brute
 *   Collection, tableau []    -> [ ... ]
 *   Map                       -> { "cle": valeur, ... }
 *   objet                     -> { "champ": valeur, ... }
 *
 * Les champs utilises sont les champs declares de l'objet, ce qui
 * reste coherent avec la facon dont Repository lit et ecrit la base.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String toJson(Object value) {
        StringBuilder sb = new StringBuilder();
        write(sb, value, 0);
        return sb.toString();
    }

    private static void write(StringBuilder sb, Object value, int depth) {
        // Garde-fou contre les references circulaires
        if (depth > 20) {
            sb.append("\"...\"");
            return;
        }

        if (value == null) {
            sb.append("null");
        } else if (value instanceof String || value instanceof Character) {
            writeString(sb, value.toString());
        } else if (value instanceof Enum<?>) {
            writeString(sb, ((Enum<?>) value).name());
        } else if (value instanceof Boolean || value instanceof Number) {
            sb.append(value.toString());
        } else if (value instanceof Collection<?>) {
            writeCollection(sb, (Collection<?>) value, depth);
        } else if (value.getClass().isArray()) {
            writeArray(sb, value, depth);
        } else if (value instanceof Map<?, ?>) {
            writeMap(sb, (Map<?, ?>) value, depth);
        } else {
            writeObject(sb, value, depth);
        }
    }

    private static void writeCollection(StringBuilder sb, Collection<?> collection, int depth) {
        sb.append('[');
        boolean premier = true;
        for (Object item : collection) {
            if (!premier) {
                sb.append(',');
            }
            premier = false;
            write(sb, item, depth + 1);
        }
        sb.append(']');
    }

    private static void writeArray(StringBuilder sb, Object array, int depth) {
        sb.append('[');
        int longueur = Array.getLength(array);
        for (int i = 0; i < longueur; i++) {
            if (i > 0) {
                sb.append(',');
            }
            write(sb, Array.get(array, i), depth + 1);
        }
        sb.append(']');
    }

    private static void writeMap(StringBuilder sb, Map<?, ?> map, int depth) {
        sb.append('{');
        boolean premier = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!premier) {
                sb.append(',');
            }
            premier = false;
            writeString(sb, String.valueOf(entry.getKey()));
            sb.append(':');
            write(sb, entry.getValue(), depth + 1);
        }
        sb.append('}');
    }

    private static void writeObject(StringBuilder sb, Object objet, int depth) {
        sb.append('{');
        boolean premier = true;
        for (Class<?> classe = objet.getClass();
             classe != null && classe != Object.class;
             classe = classe.getSuperclass()) {

            for (Field field : classe.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }
                try {
                    field.setAccessible(true);
                    Object valeur = field.get(objet);
                    if (!premier) {
                        sb.append(',');
                    }
                    premier = false;
                    writeString(sb, field.getName());
                    sb.append(':');
                    write(sb, valeur, depth + 1);
                } catch (IllegalAccessException e) {
                    // champ inaccessible : on l'ignore plutot que d'echouer
                }
            }
        }
        sb.append('}');
    }

    private static void writeString(StringBuilder sb, String texte) {
        sb.append('"');
        for (int i = 0; i < texte.length(); i++) {
            char c = texte.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
    }
}
