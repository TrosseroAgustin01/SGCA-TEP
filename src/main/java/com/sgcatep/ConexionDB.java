package com.sgcatep;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {

    private static final String URL = "jdbc:mysql://172.18.0.2:3306/sgca_tep";
    private static final String USER = "root";
    private static final String PASSWORD = "desa";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";

    // Solo Instanciamos una vez la BD
    private static Connection connection = null;


    private ConexionDB() {

    }

    public static Connection getConnection() throws SQLException {
        try {
            // Si no hay conexión O, si está cerrada, crear una nueva
            if (connection == null || connection.isClosed()) {
                // Cargamos el driver MySQL
                Class.forName(DRIVER);

                // Creamos la conexión
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                System.out.println("✓ Conexión a BD exitosa: " + URL);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("✗ Driver MySQL no encontrado: " + e.getMessage());
            throw new SQLException("Driver MySQL no disponible", e);
        } catch (SQLException e) {
            System.err.println("✗ Error conectando a BD: " + e.getMessage());
            throw new SQLException("No se pudo conectar a la base de datos", e);
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("✓ Conexión cerrada correctamente");
            }
        } catch (SQLException e) {
            System.err.println("✗ Error cerrando conexión: " + e.getMessage());
        }
    }

    /**
     * Verificamos si hay una conexión activa,
     * return true si la conexión existe y está abierta
     */
    public static boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}