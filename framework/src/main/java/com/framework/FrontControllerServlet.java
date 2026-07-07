package com.framework;

import com.framework.util.Mapping;
import com.framework.util.UtilMethode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class FrontControllerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod().toUpperCase();

        HashMap<UtilMethode, Mapping> urlMapping =
                (HashMap<UtilMethode, Mapping>) getServletContext().getAttribute("urlMapping");

        try (PrintWriter out = response.getWriter()) {
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head><title>Front Controller</title></head>");
            out.println("<body>");

            if (pathInfo.equals("/") || pathInfo.isEmpty()) {
                out.println("<h1>Bienvenue sur le Framework</h1>");
                out.println("<h2>Routes enregistrees</h2>");

                if (urlMapping == null || urlMapping.isEmpty()) {
                    out.println("<p>Aucune route trouvee.</p>");
                } else {
                    out.println("<ul>");
                    for (UtilMethode cle : urlMapping.keySet()) {
                        out.println("<li>[" + cle.getHttpMethod() + "] " + cle.getUrl() + "</li>");
                    }
                    out.println("</ul>");
                }
            } else {
                UtilMethode cle = new UtilMethode(pathInfo, httpMethod);
                Mapping mapping = urlMapping.get(cle);

                if (mapping != null) {
                    try {
                        Object instance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
                        mapping.getMethod().invoke(instance);
                        out.println("<h1>Execution reussie</h1>");
                        out.println("<p>URL : " + pathInfo + "</p>");
                        out.println("<p>Methode HTTP : " + httpMethod + "</p>");
                        out.println("<p>Methode executee : " + mapping.getMethod().getName() + "</p>");
                    } catch (Exception e) {
                        throw new ServletException("Erreur lors de l'invocation de la methode " + mapping.getMethod().getName(), e);
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
