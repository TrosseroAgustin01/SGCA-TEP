package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LogAuditoriaRepository extends BaseRepository {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final SistemaRepository sistemaRepository = new SistemaRepository();

    public LogAuditoria buscarPorId(int idLog) throws SQLException {
        String sql = "SELECT * FROM LOG_AUDITORIA WHERE id_log = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idLog);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearLog(rs);
                }
            }
        }
        return null;
    }

    public List<LogAuditoria> listarTodos() throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM LOG_AUDITORIA ORDER BY fecha_hora DESC LIMIT 100";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                logs.add(mapearLog(rs));
            }
        }
        return logs;
    }

    public List<LogAuditoria> listarPorUsuario(int idUsuario) throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM LOG_AUDITORIA WHERE id_usuario = ? ORDER BY fecha_hora DESC";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLog(rs));
                }
            }
        }
        return logs;
    }

    public List<LogAuditoria> listarPorFecha(LocalDate fecha) throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM LOG_AUDITORIA WHERE DATE(fecha_hora) = ? ORDER BY fecha_hora DESC";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLog(rs));
                }
            }
        }
        return logs;
    }

    public List<LogAuditoria> listarRechazadosUltimas24Horas() throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM LOG_AUDITORIA " +
                "WHERE resultado = 'RECHAZADO' AND fecha_hora >= NOW() - INTERVAL 1 DAY " +
                "ORDER BY fecha_hora DESC";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                logs.add(mapearLog(rs));
            }
        }
        return logs;
    }

    public List<LogAuditoria> listarPorTipoEvento(String tipoEvento) throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM LOG_AUDITORIA WHERE tipo_evento = ? ORDER BY fecha_hora DESC";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tipoEvento);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLog(rs));
                }
            }
        }
        return logs;
    }

    public List<LogAuditoria> listarPorSistema(int idSistema) throws SQLException {
        List<LogAuditoria> logs = new ArrayList<>();
        String sql = "SELECT * FROM LOG_AUDITORIA WHERE id_sistema = ? ORDER BY fecha_hora DESC";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idSistema);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(mapearLog(rs));
                }
            }
        }
        return logs;
    }

    public int insertar(LogAuditoria log) throws SQLException {
        String sql = "INSERT INTO LOG_AUDITORIA (id_usuario, tipo_evento, resultado, " +
                "motivo_rechazo, detalle, id_sistema, detalles_json, direccion_ip) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (log.getUsuario() != null) {
                stmt.setInt(1, log.getUsuario().getIdUsuario());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            stmt.setString(2, log.getTipoEvento());
            stmt.setString(3, log.getResultado().name());
            stmt.setString(4, log.getMotivoRechazo());
            stmt.setString(5, log.getDetalle());

            if (log.getSistema() != null) {
                stmt.setInt(6, log.getSistema().getIdSistema());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setString(7, log.getDetallesJson());
            stmt.setString(8, log.getDireccionIp());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el log");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    log.setIdLog(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public int registrarEvento(Usuario usuario, String tipoEvento,
                               LogAuditoria.Resultado resultado, String detalle,
                               Sistema sistema) throws SQLException {
        LogAuditoria log = new LogAuditoria(usuario, tipoEvento, resultado, detalle, sistema);
        return insertar(log);
    }

    public int registrarRechazo(Usuario usuario, String tipoEvento, String motivoRechazo,
                                String detalle, Sistema sistema) throws SQLException {
        LogAuditoria log = new LogAuditoria(usuario, tipoEvento,
                LogAuditoria.Resultado.RECHAZADO, detalle, sistema);
        log.setMotivoRechazo(motivoRechazo);
        return insertar(log);
    }

    private LogAuditoria mapearLog(ResultSet rs) throws SQLException {
        Usuario usuario = null;
        int idUsuario = rs.getInt("id_usuario");
        if (!rs.wasNull()) {
            usuario = usuarioRepository.buscarPorId(idUsuario);
        }

        Sistema sistema = null;
        int idSistema = rs.getInt("id_sistema");
        if (!rs.wasNull()) {
            sistema = sistemaRepository.buscarPorId(idSistema);
        }

        return new LogAuditoria(
                rs.getInt("id_log"),
                usuario,
                rs.getTimestamp("fecha_hora").toLocalDateTime(),
                rs.getString("tipo_evento"),
                LogAuditoria.Resultado.valueOf(rs.getString("resultado")),
                rs.getString("motivo_rechazo"),
                rs.getString("detalle"),
                sistema,
                rs.getString("detalles_json"),
                rs.getString("direccion_ip")
        );
    }
}