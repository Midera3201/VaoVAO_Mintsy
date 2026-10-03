package com.framework.util;

import com.framework.annotation.ApiJson;
import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;

import java.io.File;
import java.lang.reflect.Method;
import java.util.*;

import jakarta.servlet.ServletContext;

public class Utilitaire {

    public static void getUrlAndMethod(String packageName, HashMap<UtilMethode, Mapping> urlMapping, ServletContext context) throws Exception {
        List<Class<?>> controllerClasses = listerClassesAvecAnnotation(packageName, controller.class, context);

        System.out.println("[Framework] " + controllerClasses.size() + " classe(s) @controller trouvee(s)");

        for (Class<?> controllerClass : controllerClasses) {
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    String url = annotation.url();
                    String httpMethod = annotation.method().toUpperCase();

                    UtilMethode cle = new UtilMethode(url, httpMethod);
                    boolean isJson = method.isAnnotationPresent(ApiJson.class);
                    Mapping valeur = new Mapping(controllerClass, method, isJson);

                    urlMapping.put(cle, valeur);
                    System.out.println("[Framework] Route: [" + httpMethod + "] " + url + " -> "
                            + method.getName() + (isJson ? "  (JSON)" : "  (vue)"));
                }
            }
        }
    }

    public static List<Class<?>> listerClassesAvecAnnotation(String packageName, Class<? extends java.lang.annotation.Annotation> annotation, ServletContext context) {
        List<Class<?>> result = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        String webPath = "/WEB-INF/classes/" + packagePath;

        // Methode 1 : getResourcePaths (standard Servlet API)
        Set<String> paths = context.getResourcePaths(webPath);
        if (paths != null && !paths.isEmpty()) {
            System.out.println("[Framework] getResourcePaths a trouve " + paths.size() + " entree(s) dans " + webPath);
            for (String path : paths) {
                if (path.endsWith(".class")) {
                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotation)) {
                            result.add(clazz);
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError e) {
                        System.err.println("[Framework] Classe non chargee: " + className + " - " + e.getMessage());
                    }
                }
            }
            return result;
        }

        // Methode 2 : getRealPath (fallback)
        System.out.println("[Framework] getResourcePaths a echoue pour " + webPath + ", essai getRealPath");
        String realPath = context.getRealPath(webPath);
        System.out.println("[Framework] getRealPath(" + webPath + ") = " + realPath);
        if (realPath == null) {
            System.out.println("[Framework] ERREUR: getRealPath a retourne null");
            return result;
        }

        File directory = new File(realPath);
        if (!directory.exists()) {
            System.out.println("[Framework] ERREUR: Le dossier " + realPath + " n'existe pas");
            return result;
        }

        System.out.println("[Framework] Scan du dossier: " + realPath);
        scanDirectory(directory, packageName, annotation, result);
        return result;
    }

    private static void scanDirectory(File directory, String packageName, Class<? extends java.lang.annotation.Annotation> annotation, List<Class<?>> result) {
        File[] files = directory.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, packageName + "." + file.getName(), annotation, result);
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotation)) {
                        result.add(clazz);
                    }
                } catch (ClassNotFoundException | NoClassDefFoundError e) {
                    System.err.println("[Framework] Classe non chargee: " + className + " - " + e.getMessage());
                }
            }
        }
    }
}
