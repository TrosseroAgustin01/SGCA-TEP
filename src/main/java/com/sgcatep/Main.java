package com.sgcatep;

import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        try {
            Connection conn = ConexionDB.getConnection();
            System.out.println("¡Conectado a la BD!");
            ConexionDB.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}