package com.sgcatep.controller;

import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.VisibilidadService;
import com.sgcatep.model.service.VisibilidadService.Alcance;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConsultaController {

    public record Vista(String alcance, List<Fichada> fichadas) {}

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final VisibilidadService visibilidad = new VisibilidadService();

    public Vista fichadasVisibles(Usuario usuario) throws SQLException {
        VisibilidadService.Visibilidad v = visibilidad.fichadasVisibles(usuario);
        return new Vista(etiqueta(v.alcance()), v.fichadas());
    }

    public static String linea(Fichada f) {
        return "%s | %s, %s | %s | %s".formatted(
                f.getFechaHora().format(FMT),
                f.getUsuario().getApellido(), f.getUsuario().getNombre(),
                f.getTipo(), f.getModalidad());
    }

    private String etiqueta(Alcance alcance) {
        return switch (alcance) {
            case PROPIO -> "Alcance: solo sus fichadas";
            case AREA -> "Alcance: fichadas de sus áreas de supervisión";
            case TODAS -> "Alcance: fichadas de todas las áreas (RH)";
        };
    }
}