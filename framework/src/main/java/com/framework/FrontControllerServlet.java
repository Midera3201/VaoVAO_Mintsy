package com.framework;

import com.framework.util.Mapping;
import com.framework.util.Model;
import com.framework.util.ModelAndView;
import com.framework.util.UtilMethode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class FrontControllerServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        String pathInfo = request.getRequestURI().substring(request.getContextPath().length());
        String httpMethod = request.getMethod().toUpperCase();

        HashMap<UtilMethode, Mapping> urlMapping =
                (HashMap<UtilMethode, Mapping>) getServletContext().getAttribute("urlMapping");

        if (pathInfo.equals("/") || pathInfo.isEmpty()) {
            PrintWriter out = response.getWriter();
            out.println("<h1>Bienvenue sur le Framework</h1>");
            if (urlMapping == null || urlMapping.isEmpty()) {
                out.println("<p style='color:red'>Aucune route enregistree.</p>");
            } else {
                out.println("<h2>Routes enregistrees</h2><ul>");
                for (UtilMethode cle : urlMapping.keySet()) {
                    out.println("<li>[" + cle.getHttpMethod() + "] " + cle.getUrl() + "</li>");
                }
                out.println("</ul>");
            }
            return;
        }

        UtilMethode cle = new UtilMethode(pathInfo, httpMethod);
        Mapping mapping = urlMapping.get(cle);

        if (mapping == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Aucune methode trouvee pour l'URL '" + pathInfo + "' avec la methode HTTP " + httpMethod);
            return;
        }

        try {
            Object instance = mapping.getControllerClass().getDeclaredConstructor().newInstance();
            Method method = mapping.getMethod();

            Model model = new Model();
            Object[] args = buildMethodArgs(method, model, request, response);
            String viewName = (String) method.invoke(instance, args);

            String prefix = (String) getServletContext().getAttribute("viewPrefix");
            String suffix = (String) getServletContext().getAttribute("viewSuffix");

            ModelAndView mav = new ModelAndView(viewName);
            for (Map.Entry<String, Object> entry : model.getData().entrySet()) {
                mav.addObject(entry.getKey(), entry.getValue());
                request.setAttribute(entry.getKey(), entry.getValue());
            }

            String viewPath = (prefix != null ? prefix : "") + viewName + (suffix != null ? suffix : "");
            request.getRequestDispatcher(viewPath).forward(request, response);

        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'invocation de la methode " + mapping.getMethod().getName(), e);
        }
    }

    private Object[] buildMethodArgs(Method method, Model model, HttpServletRequest request, HttpServletResponse response) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return new Object[0];
        }
        Object[] args = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            if (paramTypes[i] == Model.class) {
                args[i] = model;
            } else if (paramTypes[i] == HttpServletRequest.class) {
                args[i] = request;
            } else if (paramTypes[i] == HttpServletResponse.class) {
                args[i] = response;
            }
        }
        return args;
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
