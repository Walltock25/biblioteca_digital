package com.biblioteca.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final DatabaseConnection INSTANCE = new DatabaseConnection();

    private String url;
    private String username;
    private String password;

    private DatabaseConnection() {
        loadDatabaseProperties();
    }

    public static DatabaseConnection getInstance() {
        return INSTANCE;
    }

    private void loadDatabaseProperties() {
        Properties props = new Properties();

        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("database.properties")) {

            if (input == null) {
                throw new RuntimeException("No se encontró database.properties");
            }

            props.load(input);

            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");

            // Cargar el driver JDBC
            Class.forName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

        } catch (IOException e) {
            throw new RuntimeException("Error al cargar database.properties: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver JDBC no encontrado: " + e.getMessage(), e);
        }
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            // Configuraciones recomendadas
            conn.setAutoCommit(true);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return conn;
        } catch (SQLException e) {
            throw new SQLException("Error al conectar a la base de datos: " + e.getMessage(), e);
        }
    }

    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                // Log del error
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}