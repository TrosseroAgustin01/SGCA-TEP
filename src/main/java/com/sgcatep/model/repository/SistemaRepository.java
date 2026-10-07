package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.Sistema;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SistemaRepository {

    public Sistema buscarPorId(int idSistema) throws SQLException {
        String sql = "SELECT * FROM SISTEMA WHERE id_sistema = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSistema);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearSistema(rs);
                }
            }
        }
        return null;
    }

    public Sistema buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM SISTEMA WHERE nombre_sistema = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearSistema(rs);
                }
            }
        }
        return null;
    }

    public List<Sistema> listarTodos() throws SQLException {
        List<Sistema> sistemas = new ArrayList<>();
        String sql = "SELECT * FROM SISTEMA ORDER BY nombre_sistema";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sistemas.add(mapearSistema(rs));
            }
        }
        return sistemas;
    }

    public List<Sistema> listarActivos() throws SQLException {
        List<Sistema> sistemas = new ArrayList<>();
        String sql = "SELECT * FROM SISTEMA WHERE activo = TRUE ORDER BY nombre_sistema";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                sistemas.add(mapearSistema(rs));
            }
        }
        return sistemas;
    }

    public int insertar(Sistema sistema) throws SQLException {
        String sql = "INSERT INTO SISTEMA (nombre_sistema, url_endpoint, descripcion, activo) VALUES (?, ?, ?, ?)";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sistema.getNombre());
            stmt.setString(2, sistema.getUrlEndpoint());
            stmt.setString(3, sistema.getDescripcion());
            stmt.setBoolean(4, true);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el sistema");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    sistema.setIdSistema(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public boolean actualizar(Sistema sistema) throws SQLException {
        String sql = "UPDATE SISTEMA SET nombre_sistema = ?, url_endpoint = ?, descripcion = ? WHERE id_sistema = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sistema.getNombre());
            stmt.setString(2, sistema.getUrlEndpoint());
            stmt.setString(3, sistema.getDescripcion());
            stmt.setInt(4, sistema.getIdSistema());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int idSistema) throws SQLException {
        String sql = "UPDATE SISTEMA SET activo = FALSE WHERE id_sistema = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSistema);
            return stmt.executeUpdate() > 0;
        }
    }

    public List<Sistema> listarSistemasDeUsuario(int idUsuario) throws SQLException {
        List<Sistema> sistemas = new ArrayList<>();
        String sql = "SELECT DISTINCT s.* " +
                "FROM SISTEMA s " +
                "JOIN PERMISO_SISTEMA ps ON s.id_sistema = ps.id_sistema " +
                "JOIN ROL_PERMISO rp ON ps.id_permiso = rp.id_permiso " +
                "JOIN USUARIO_ROL ur ON rp.id_rol = ur.id_rol " +
                "WHERE ur.id_usuario = ? AND s.activo = TRUE " +
                "ORDER BY s.nombre_sistema";

        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    sistemas.add(mapearSistema(rs));
                }
            }
        }
        return sistemas;
    }

    private Sistema mapearSistema(ResultSet rs) throws SQLException {
        return new Sistema(
                rs.getInt("id_sistema"),
                rs.getString("nombre_sistema"),
                rs.getString("url_endpoint"),
                rs.getString("descripcion")
        );
    }
}