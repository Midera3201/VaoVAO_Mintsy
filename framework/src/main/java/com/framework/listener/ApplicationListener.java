package com.framework.listener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;

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

            System.out.println("Framework initialise - " + urlMapping.size() + " route(s) trouvee(s)");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

}
