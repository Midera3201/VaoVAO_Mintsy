package com.framework;

import com.framework.annotation.UrlMapping;
import com.framework.annotation.controller;
import com.framework.util.utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.*;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllerClasses = new ArrayList<>();
    private Map<String, Map<String, Method>> urlMethodMap = new HashMap<>();

    @Override
    public void init() throws ServletException {
        String controllerPackage = getInitParameter("controller-package");

        if (controllerPackage == null || controllerPackage.isBlank()) {
            controllerPackage = "controller";
        }

        try {
            controllerClasses = utilitaire.listerClassesAvecAnnotation(controllerPackage, controller.class);
            buildUrlMethodMap();
        } catch (IOException e) {
            throw new ServletException("Erreur pendant le scan des controllers", e);
        }
    }

    private void buildUrlMethodMap() throws ServletException {
        for (Class<?> controllerClass : controllerClasses) {
            for (Method method : controllerClass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping mapping = method.getAnnotation(UrlMapping.class);
                    String url = mapping.url();
                    String httpMethod = mapping.method().toUpperCase();

                    urlMethodMap.computeIfAbsent(url, k -> new HashMap<>())
                                .put(httpMethod, method);
                }
            }
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod().toUpperCase();

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Front Controller</title></head>");
            out.println("<body>");

            if (pathInfo.equals("/") || pathInfo.isEmpty()) {
                out.println("<h1>Bienvenue sur le Framework</h1>");
                out.println("<h2>Classes avec annotation @controller</h2>");

                if (controllerClasses.isEmpty()) {
                    out.println("<p>Aucune classe annotee trouvee.</p>");
                } else {
                    out.println("<ul>");
                    for (Class<?> controllerClass : controllerClasses) {
                        out.println("<li>" + controllerClass.getName() + "</li>");
                    }
                    out.println("</ul>");
                }
            } else {
                Map<String, Method> methodMap = urlMethodMap.get(pathInfo);
                if (methodMap != null && methodMap.containsKey(httpMethod)) {
                    Method method = methodMap.get(httpMethod);
                    Class<?> declaringClass = method.getDeclaringClass();
                    try {
                        Object instance = declaringClass.getDeclaredConstructor().newInstance();
                        method.invoke(instance);
                        out.println("<h1>Execution reussie</h1>");
                        out.println("<p>URL : " + pathInfo + "</p>");
                        out.println("<p>Methode HTTP : " + httpMethod + "</p>");
                        out.println("<p>Methode executee : " + method.getName() + "</p>");
                    } catch (Exception e) {
                        throw new ServletException("Erreur lors de l'invocation de la methode " + method.getName(), e);
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                            "Aucune methode trouvee pour l'URL '" + pathInfo + "' avec la methode HTTP " + httpMethod);
                }
            }

            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
