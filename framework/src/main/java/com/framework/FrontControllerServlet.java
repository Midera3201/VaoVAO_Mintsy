package com.framework;

import com.framework.annotation.controller;
import com.framework.util.utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Front Controller du framework MVC.
 * Toutes les requetes de l'application passent par ce servlet
 * (declaration dans le web.xml de l'application avec url-pattern "/*").
 */
public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> controllerClasses = new ArrayList<>();

    @Override
    public void init() throws ServletException {
        String controllerPackage = getInitParameter("controller-package");

        if (controllerPackage == null || controllerPackage.isBlank()) {
            controllerPackage = "controller";
        }

        try {
            controllerClasses = utilitaire.listerClassesAvecAnnotation(controllerPackage, controller.class);
        } catch (IOException e) {
            throw new ServletException("Erreur pendant le scan des controllers", e);
        }
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        // Récupérer l'URL tapée (ex: /aaa)
        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Front Controller</title></head>");
            out.println("<body>");

            // Resultat attendu : si /aaa, on extrait "aaa"
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
                // On enleve le "/" au debut pour afficher juste "aaa"
                String pageName = pathInfo.substring(1);
                out.println("<h1>Navigation : " + pathInfo + " affiche " + pageName + "</h1>");
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
