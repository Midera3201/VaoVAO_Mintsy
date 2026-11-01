package com.framework.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.framework.util.ControllerRegistry;
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

            String dbDriver = context.getInitParameter("db.driver");
            String dbUrl = context.getInitParameter("db.url");
            String dbUser = context.getInitParameter("db.user");
            String dbPassword = context.getInitParameter("db.password");
            if (dbDriver != null && dbUrl != null) {
                DatabaseConfig.init(dbDriver, dbUrl, dbUser, dbPassword);
                runInitSql();
            }

            // Sprint 11 : une seule instance par classe de controleur.
            // Toutes les routes d'un meme controleur partagent cette instance.
            // L'instanciation a lieu apres l'initialisation de la base pour que
            // les constructeurs puissent utiliser la configuration du framework.
            Set<Class<?>> classesControleurs = new LinkedHashSet<>();
            for (Mapping mapping : urlMapping.values()) {
                classesControleurs.add(mapping.getControllerClass());
            }

            Map<Class<?>, Object> instancesControleurs =
                    ControllerRegistry.creerInstances(classesControleurs);
            ControllerRegistry.enregistrer(context, instancesControleurs);

            System.out.println("[Framework] " + instancesControleurs.size()
                    + " controleur(s) instancie(s) une seule fois");

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
        // Libere les references vers les controleurs lors de l'arret de
        // l'application (utile pour le redeploiement et le ramasse-miettes).
        sce.getServletContext().removeAttribute(ControllerRegistry.CONTEXT_ATTRIBUT);
    }

}
