package com.framework.util;

import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

public class Utilitaire {

    public static void getUrlAndMethod(String packageName, HashMap<UtilMethode, Mapping> urlMapping) throws Exception {
        List<Class<?>> controllerClasses = listerClassesAvecAnnotation(packageName, controller.class);

        for (Class<?> controllerClass : controllerClasses) {
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                    String url = annotation.url();
                    String httpMethod = annotation.method().toUpperCase();

                    UtilMethode cle = new UtilMethode(url, httpMethod);
                    Mapping valeur = new Mapping(controllerClass, method);

                    urlMapping.put(cle, valeur);
                }
            }
        }
    }

    private static List<Class<?>> listerClassesAvecAnnotation(String packageName, Class<? extends java.lang.annotation.Annotation> annotation) throws IOException {
        List<Class<?>> result = new ArrayList<>();
        String path = packageName.replace('.', '/');

        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.getFile());
            if (directory.exists()) {
                scanDirectory(directory, packageName, annotation, result);
            }
        }
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
                    // skip si la classe n'est pas chargeable
                }
            }
        }
    }
}
