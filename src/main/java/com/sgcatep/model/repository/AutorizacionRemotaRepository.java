package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.AutorizacionRemota;
import com.sgcatep.model.domain.Usuario;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AutorizacionRemotaRepository {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();

    public AutorizacionRemota buscarPorId(int idAutorizacion) throws SQLException {
        String sql = "SELECT * FROM AUTORIZACION_REMOTA WHERE id_autorizacion = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAutorizacion);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearAutorizacion(rs);
                }
            }
        }
        return null;
    }

    public List<AutorizacionRemota> listarTodas() throws SQLException {
        List<AutorizacionRemota> autorizaciones = new ArrayList<>();
        String sql = "SELECT * FROM AUTORIZACION_REMOTA ORDER BY fecha_fin DESC";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                autorizaciones.add(mapearAutorizacion(rs));
            }
        }
        return autorizaciones;
    }

    public List<AutorizacionRemota> listarPorUsuario(int idUsuario) throws SQLException {
        List<AutorizacionRemota> autorizaciones = new ArrayList<>();
        String sql = "SELECT * FROM AUTORIZACION_REMOTA WHERE id_usuario = ? ORDER BY fecha_fin DESC";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autorizaciones.add(mapearAutorizacion(rs));
                }
            }
        }
        return autorizaciones;
    }

    public AutorizacionRemota obtenerVigenteDeUsuario(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM AUTORIZACION_REMOTA " +
                "WHERE id_usuario = ? AND activa = TRUE " +
                "AND CURDATE() BETWEEN fecha_inicio AND fecha_fin " +
                "ORDER BY fecha_fin DESC LIMIT 1";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearAutorizacion(rs);
                }
            }
        }
        return null;
    }

    public List<AutorizacionRemota> listarVigentes() throws SQLException {
        List<AutorizacionRemota> autorizaciones = new ArrayList<>();
        String sql = "SELECT * FROM AUTORIZACION_REMOTA " +
                "WHERE activa = TRUE AND CURDATE() BETWEEN fecha_inicio AND fecha_fin " +
                "ORDER BY fecha_fin DESC";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                autorizaciones.add(mapearAutorizacion(rs));
            }
        }
        return autorizaciones;
    }

    public List<AutorizacionRemota> listarExpiradas() throws SQLException {
        List<AutorizacionRemota> autorizaciones = new ArrayList<>();
        String sql = "SELECT * FROM AUTORIZACION_REMOTA " +
                "WHERE CURDATE() > fecha_fin " +
                "ORDER BY fecha_fin DESC";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                autorizaciones.add(mapearAutorizacion(rs));
            }
        }
        return autorizaciones;
    }

    public int insertar(AutorizacionRemota autorizacion) throws SQLException {
        String sql = "INSERT INTO AUTORIZACION_REMOTA (id_usuario, fecha_inicio, fecha_fin, " +
                "motivo, activa, creada_por) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, autorizacion.getUsuario().getIdUsuario());
            stmt.setDate(2, Date.valueOf(autorizacion.getFechaInicio()));
            stmt.setDate(3, Date.valueOf(autorizacion.getFechaFin()));
            stmt.setString(4, autorizacion.getMotivo());
            stmt.setBoolean(5, autorizacion.isActiva());
            stmt.setInt(6, autorizacion.getCreadaPor().getIdUsuario());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar la autorización");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    autorizacion.setIdAutorizacion(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public boolean actualizar(AutorizacionRemota autorizacion) throws SQLException {
        String sql = "UPDATE AUTORIZACION_REMOTA SET fecha_inicio = ?, fecha_fin = ?, " +
                "motivo = ?, activa = ? WHERE id_autorizacion = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(autorizacion.getFechaInicio()));
            stmt.setDate(2, Date.valueOf(autorizacion.getFechaFin()));
            stmt.setString(3, autorizacion.getMotivo());
            stmt.setBoolean(4, autorizacion.isActiva());
            stmt.setInt(5, autorizacion.getIdAutorizacion());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int idAutorizacion) throws SQLException {
        String sql = "UPDATE AUTORIZACION_REMOTA SET activa = FALSE WHERE id_autorizacion = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAutorizacion);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int idAutorizacion) throws SQLException {
        String sql = "DELETE FROM AUTORIZACION_REMOTA WHERE id_autorizacion = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAutorizacion);
            return stmt.executeUpdate() > 0;
        }
    }

    private AutorizacionRemota mapearAutorizacion(ResultSet rs) throws SQLException {
        int idUsuario = rs.getInt("id_usuario");
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario);

        int idCreadaPor = rs.getInt("creada_por");
        Usuario creadaPor = usuarioRepository.buscarPorId(idCreadaPor);

        return new AutorizacionRemota(
                rs.getInt("id_autorizacion"),
                usuario,
                rs.getDate("fecha_inicio").toLocalDate(),
                rs.getDate("fecha_fin").toLocalDate(),
                rs.getString("motivo"),
                rs.getBoolean("activa"),
                creadaPor,
                rs.getTimestamp("fecha_creacion").toLocalDateTime()
        );
    }
}