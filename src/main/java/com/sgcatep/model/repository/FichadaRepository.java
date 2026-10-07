package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.AutorizacionRemota;
import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.Usuario;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FichadaRepository {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final AutorizacionRemotaRepository autorizacionRepository = new AutorizacionRemotaRepository();

    public Fichada buscarPorId(int idFichada) throws SQLException {
        String sql = "SELECT * FROM FICHADA WHERE id_fichada = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFichada);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFichada(rs);
                }
            }
        }
        return null;
    }

    public List<Fichada> listarTodas() throws SQLException {
        List<Fichada> fichadas = new ArrayList<>();
        String sql = "SELECT * FROM FICHADA ORDER BY fecha_hora DESC";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                fichadas.add(mapearFichada(rs));
            }
        }
        return fichadas;
    }

    public List<Fichada> listarPorUsuario(int idUsuario) throws SQLException {
        List<Fichada> fichadas = new ArrayList<>();
        String sql = "SELECT * FROM FICHADA WHERE id_usuario = ? ORDER BY fecha_hora DESC";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fichadas.add(mapearFichada(rs));
                }
            }
        }
        return fichadas;
    }

    public List<Fichada> listarPorUsuarioYFecha(int idUsuario, LocalDate fecha) throws SQLException {
        List<Fichada> fichadas = new ArrayList<>();
        String sql = "SELECT * FROM FICHADA WHERE id_usuario = ? AND DATE(fecha_hora) = ? ORDER BY fecha_hora";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setDate(2, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fichadas.add(mapearFichada(rs));
                }
            }
        }
        return fichadas;
    }

    public List<Fichada> listarPorArea(int idArea) throws SQLException {
        List<Fichada> fichadas = new ArrayList<>();
        String sql = "SELECT f.* FROM FICHADA f " +
                "JOIN USUARIO u ON f.id_usuario = u.id_usuario " +
                "WHERE u.id_area = ? " +
                "ORDER BY f.fecha_hora DESC";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idArea);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    fichadas.add(mapearFichada(rs));
                }
            }
        }
        return fichadas;
    }

    public Fichada obtenerUltimaFichadaDelDia(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM FICHADA WHERE id_usuario = ? AND DATE(fecha_hora) = CURDATE() " +
                "ORDER BY fecha_hora DESC LIMIT 1";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearFichada(rs);
                }
            }
        }
        return null;
    }

    public boolean tieneEntradaHoy(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM FICHADA WHERE id_usuario = ? AND tipo = 'ENTRADA' " +
                "AND DATE(fecha_hora) = CURDATE()";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int insertar(Fichada fichada) throws SQLException {
        String sql = "INSERT INTO FICHADA (id_usuario, tipo, modalidad, fecha_hora, " +
                "id_autorizacion_remota, observaciones) VALUES (?, ?, ?, ?, ?, ?)";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, fichada.getUsuario().getIdUsuario());
            stmt.setString(2, fichada.getTipo().name());
            stmt.setString(3, fichada.getModalidad().name());
            stmt.setTimestamp(4, Timestamp.valueOf(fichada.getFechaHora()));

            if (fichada.getAutorizacionRemota() != null) {
                stmt.setInt(5, fichada.getAutorizacionRemota().getIdAutorizacion());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setString(6, fichada.getObservaciones());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar la fichada");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    fichada.setIdFichada(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public boolean eliminar(int idFichada) throws SQLException {
        String sql = "DELETE FROM FICHADA WHERE id_fichada = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idFichada);
            return stmt.executeUpdate() > 0;
        }
    }

    private Fichada mapearFichada(ResultSet rs) throws SQLException {
        int idUsuario = rs.getInt("id_usuario");
        Usuario usuario = usuarioRepository.buscarPorId(idUsuario);

        AutorizacionRemota autorizacion = null;
        int idAutorizacion = rs.getInt("id_autorizacion_remota");
        if (!rs.wasNull()) {
            autorizacion = autorizacionRepository.buscarPorId(idAutorizacion);
        }

        return new Fichada(
                rs.getInt("id_fichada"),
                usuario,
                Fichada.Tipo.valueOf(rs.getString("tipo")),
                Fichada.Modalidad.valueOf(rs.getString("modalidad")),
                rs.getTimestamp("fecha_hora").toLocalDateTime(),
                autorizacion,
                rs.getString("observaciones")
        );
    }
}