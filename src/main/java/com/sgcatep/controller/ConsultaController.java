package com.sgcatep.controller;

import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AreaRepository;
import com.sgcatep.model.service.VisibilidadService;
import com.sgcatep.model.service.VisibilidadService.Alcance;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultaController {

    public static final int TAMANIO = 10;

    public record Vista(String alcance, List<Fichada> fichadas) {}
    public record PaginaAuditoria(String alcance, int total, List<String> lineas) {}

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VisibilidadService visibilidad = new VisibilidadService();
    private final AreaRepository areaRepository = new AreaRepository();

    public Vista fichadasVisibles(Usuario usuario) throws SQLException {
        VisibilidadService.Visibilidad v = visibilidad.fichadasVisibles(usuario);
        return new Vista(etiqueta(v.alcance()), v.fichadas());
    }

    public PaginaAuditoria auditoria(Usuario usuario, Integer idArea, int pagina) throws SQLException {
        VisibilidadService.PaginaLogs p = visibilidad.logsVisibles(usuario, idArea, pagina, TAMANIO);
        List<String> lineas = p.registros().stream()
                .map(ConsultaController::lineaLog)
                .toList();
        return new PaginaAuditoria(etiqueta(p.alcance()), p.total(), lineas);
    }

    public List<Area> areas() throws SQLException {
        return areaRepository.listarActivas();
    }

    public static String linea(Fichada f) {
        return "%s | %s, %s | %s | %s".formatted(
                f.getFechaHora().format(FMT),
                f.getUsuario().getApellido(), f.getUsuario().getNombre(),
                f.getTipo(), f.getModalidad());
    }

    public static String lineaLog(LogAuditoria l) {
        String quien = l.getUsuario() == null ? "sin usuario"
                : l.getUsuario().getApellido() + ", " + l.getUsuario().getNombre();
        String motivo = l.getMotivoRechazo() == null ? "" : " (" + l.getMotivoRechazo() + ")";
        return "%s | %s | %s | %s%s | %s".formatted(
                l.getFechaHora().format(FMT), l.getTipoEvento(), quien,
                l.getResultado(), motivo, l.getDetalle());
    }

    private String etiqueta(Alcance alcance) {
        return switch (alcance) {
            case PROPIO -> "Alcance: solo sus registros";
            case AREA -> "Alcance: registros de sus áreas de supervisión";
            case TODAS -> "Alcance: registros de todas las áreas (RH)";
        };
    }
}