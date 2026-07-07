package com.framework.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class utilitaire {

    public static List<Class<?>> listerClassesAvecAnnotation(String packageName, Class<? extends Annotation> annotation) throws IOException {
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

    private static void scanDirectory(File directory, String packageName, Class<? extends Annotation> annotation, List<Class<?>> result) {
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
