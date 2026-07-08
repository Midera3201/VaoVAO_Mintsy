package com.framework.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConfig {
    private static String driver;
    private static String url;
    private static String user;
    private static String password;

    public static void init(String driver, String url, String user, String password) {
        DatabaseConfig.driver = driver;
        DatabaseConfig.url = url;
        DatabaseConfig.user = user;
        DatabaseConfig.password = password;
    }

    public static Connection getConnection() throws Exception {
        Class.forName(driver);
        return DriverManager.getConnection(url, user, password);
    }
}
