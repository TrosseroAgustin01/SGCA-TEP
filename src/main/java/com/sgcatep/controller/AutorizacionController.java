package com.sgcatep.controller;

import com.sgcatep.exception.AutorizacionRemotaException;
import com.sgcatep.model.domain.AutorizacionRemota;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AutorizacionRemotaRepository;
import com.sgcatep.model.repository.UsuarioRepository;
import com.sgcatep.model.service.GestorAutorizacion;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AutorizacionController {

    public record Resultado(boolean ok, String mensaje) {}

    private interface Operacion {
        void ejecutar() throws SQLException, AutorizacionRemotaException;
    }

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final AutorizacionRemotaRepository autorizacionRepository = new AutorizacionRemotaRepository();
    private final GestorAutorizacion gestor = new GestorAutorizacion();

    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioRepository.listarActivos();
    }

    public List<AutorizacionRemota> listarDe(int idUsuario) throws SQLException {
        return autorizacionRepository.listarPorUsuario(idUsuario);
    }

    public Resultado alta(Usuario admin, int idDestino, LocalDate inicio, LocalDate fin, String motivo) {
        return correr("Autorización registrada.",
                () -> gestor.alta(admin, idDestino, inicio, fin, motivo));
    }

    public Resultado desactivar(Usuario admin, int idAutorizacion) {
        return correr("Autorización desactivada.",
                () -> gestor.desactivar(admin, idAutorizacion));
    }

    public String linea(AutorizacionRemota a) {
        return "%s al %s | %s | %s".formatted(
                a.getFechaInicio().format(FMT), a.getFechaFin().format(FMT),
                a.getMotivo(), estado(a));
    }

    public String estado(AutorizacionRemota a) {
        LocalDate hoy = LocalDate.now();
        if (!a.isActiva()) {
            return "inactiva";
        }
        if (hoy.isBefore(a.getFechaInicio())) {
            return "pendiente";
        }
        if (hoy.isAfter(a.getFechaFin())) {
            return "expirada";
        }
        return "vigente";
    }

    private Resultado correr(String okMensaje, Operacion op) {
        try {
            op.ejecutar();
            return new Resultado(true, "✓ " + okMensaje);
        } catch (AutorizacionRemotaException ex) {
            return new Resultado(false, "✗ " + ex.getMessage());
        } catch (SQLException ex) {
            return new Resultado(false, "Error de base de datos: " + ex.getMessage());
        }
    }
}