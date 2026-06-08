package com.transport.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton gérant la connexion MySQL pour l'application Transport.
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/transport_db?useSSL=false&serverTimezone=Africa/Douala&allowPublicKeyRetrieval=true";
    private static final String USER     = "root";
    private static final String PASSWORD = "";   // ← Modifier selon votre config MySQL

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("[DB] Connexion MySQL établie.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] Driver MySQL introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("[DB] Erreur connexion MySQL : " + e.getMessage());
        }
    }

    public static DatabaseConnection getInstance() {
        if (instance == null || !isConnected()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private static boolean isConnected() {
        try {
            return instance != null && instance.connection != null && !instance.connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public Connection getConnection() {
        if (!isConnected()) {
            instance = new DatabaseConnection();
        }
        return connection;
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[DB] Connexion MySQL fermée.");
            }
        } catch (SQLException e) {
            System.err.println("[DB] Erreur fermeture : " + e.getMessage());
        }
    }
}
