package com.framework.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;

import com.framework.util.Authentification;
import com.framework.util.DatabaseConfig;
import com.framework.util.Mapping;
import com.framework.util.UtilMethode;
import com.framework.util.Utilitaire;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class ApplicationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {

            ServletContext context = sce.getServletContext();

            String pack = context.getInitParameter("controller");

            HashMap<UtilMethode, Mapping> urlMapping = new HashMap<>();

            Utilitaire.getUrlAndMethod(pack, urlMapping, context);

            context.setAttribute("viewPrefix",
                    context.getInitParameter("viewPrefix"));

            context.setAttribute("viewSuffix",
                    context.getInitParameter("viewSuffix"));

            context.setAttribute("urlMapping", urlMapping);

            // Sprint 10 : configuration de la verification d'authentification.
            // Le nom de l'attribut de session est configurable dans web.xml ;
            // "utilisateur" est utilise par defaut.
            String attributAuth = context.getInitParameter(
                    Authentification.PARAM_ATTRIBUT_SESSION);
            if (attributAuth == null || attributAuth.isBlank()) {
                attributAuth = Authentification.ATTRIBUT_PAR_DEFAUT;
            }
            context.setAttribute(Authentification.CONTEXT_ATTRIBUT_SESSION,
                    attributAuth.trim());

            String attributRole = context.getInitParameter(
                    Authentification.PARAM_ATTRIBUT_ROLE);
            if (attributRole == null || attributRole.isBlank()) {
                attributRole = Authentification.ATTRIBUT_ROLE_PAR_DEFAUT;
            }
            context.setAttribute(Authentification.CONTEXT_ATTRIBUT_ROLE,
                    attributRole.trim());

            String loginUrl = context.getInitParameter(Authentification.PARAM_LOGIN_URL);
            if (loginUrl != null && !loginUrl.isBlank()) {
                context.setAttribute(Authentification.CONTEXT_LOGIN_URL, loginUrl.trim());
            }

            System.out.println("[Framework] Authentification : attribut de session = '"
                    + attributAuth.trim() + "', attribut de role = '"
                    + attributRole.trim() + "'"
                    + (loginUrl != null && !loginUrl.isBlank()
                            ? ", redirection vers " + loginUrl.trim()
                            : ", refus en HTTP 401"));

            String dbDriver = context.getInitParameter("db.driver");
            String dbUrl = context.getInitParameter("db.url");
            String dbUser = context.getInitParameter("db.user");
            String dbPassword = context.getInitParameter("db.password");
            if (dbDriver != null && dbUrl != null) {
                DatabaseConfig.init(dbDriver, dbUrl, dbUser, dbPassword);
                runInitSql();
            }

            System.out.println("Framework initialise - " + urlMapping.size() + " route(s) trouvee(s)");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Execute le script init.sql present dans le classpath.
     * Chaque instruction se termine par un point-virgule ; les lignes
     * vides et les commentaires (--) sont ignores.
     */
    private void runInitSql() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("init.sql")) {
            if (in == null) {
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sql.append(line).append(" ");
                if (line.endsWith(";")) {
                    try (Connection conn = DatabaseConfig.getConnection();
                         Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate(sql.toString());
                    }
                    sql.setLength(0);
                }
            }
            System.out.println("[Framework] Donnees initiales inserees depuis init.sql");
        } catch (Exception e) {
            System.err.println("[Framework] Erreur init.sql: " + e.getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
