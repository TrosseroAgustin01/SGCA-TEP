package com.sgcatep.model.service;

import com.sgcatep.exception.AutorizacionRemotaException;
import com.sgcatep.exception.SolapamientoAutorizacionException;
import com.sgcatep.model.domain.AutorizacionRemota;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Permiso;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AutorizacionRemotaRepository;
import com.sgcatep.model.repository.LogAuditoriaRepository;
import com.sgcatep.model.repository.PermisoRepository;
import com.sgcatep.model.repository.UsuarioRepository;

import java.sql.SQLException;
import java.time.LocalDate;

public class GestorAutorizacion {

    private static final String PERMISO_REMOTO = "Autorizar remoto";

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PermisoRepository permisoRepository = new PermisoRepository();
    private final AutorizacionRemotaRepository autorizacionRepository = new AutorizacionRemotaRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();

    public void alta(Usuario admin, int idDestino, LocalDate inicio, LocalDate fin, String motivo)
            throws SQLException, AutorizacionRemotaException {
        verificarAdmin(admin, "ALTA_AUTORIZACION_REMOTA");

        Usuario destino = usuarioRepository.buscarPorId(idDestino);
        if (destino == null) {
            rechazar(admin, "ALTA_AUTORIZACION_REMOTA", "USUARIO_NO_EXISTE",
                    new AutorizacionRemotaException("El usuario destino no existe."));
        }
        if (!destino.isActivo()) {
            rechazar(admin, "ALTA_AUTORIZACION_REMOTA", "USUARIO_INACTIVO",
                    new AutorizacionRemotaException("El usuario destino está inactivo."));
        }
        if (inicio == null || fin == null || motivo == null || motivo.isBlank()) {
            rechazar(admin, "ALTA_AUTORIZACION_REMOTA", "DATOS_INVALIDOS",
                    new AutorizacionRemotaException("Complete fechas y motivo."));
        }
        if (fin.isBefore(inicio)) {
            rechazar(admin, "ALTA_AUTORIZACION_REMOTA", "FECHAS_INVALIDAS",
                    new AutorizacionRemotaException("La fecha de fin es anterior a la de inicio."));
        }
        if (fin.isBefore(LocalDate.now())) {
            rechazar(admin, "ALTA_AUTORIZACION_REMOTA", "FECHA_PASADA",
                    new AutorizacionRemotaException("La fecha de fin ya pasó."));
        }

        boolean solapa = autorizacionRepository.listarPorUsuario(idDestino).stream()
                .filter(AutorizacionRemota::isActiva)
                .anyMatch(a -> !fin.isBefore(a.getFechaInicio()) && !inicio.isAfter(a.getFechaFin()));
        if (solapa) {
            rechazar(admin, "ALTA_AUTORIZACION_REMOTA", "SOLAPAMIENTO",
                    new SolapamientoAutorizacionException(
                            "Ya existe una autorización activa que se superpone con ese período."));
        }

        AutorizacionRemota nueva = new AutorizacionRemota(destino, inicio, fin, motivo.trim(), admin);
        autorizacionRepository.insertar(nueva);
        logRepository.registrarEvento(admin, "ALTA_AUTORIZACION_REMOTA", LogAuditoria.Resultado.AUTORIZADO,
                "Destino: " + idDestino + ", Período: " + inicio + " a " + fin, null);
    }

    public void desactivar(Usuario admin, int idAutorizacion)
            throws SQLException, AutorizacionRemotaException {
        verificarAdmin(admin, "DESACTIVACION_AUTORIZACION_REMOTA");

        AutorizacionRemota autorizacion = autorizacionRepository.buscarPorId(idAutorizacion);
        if (autorizacion == null) {
            rechazar(admin, "DESACTIVACION_AUTORIZACION_REMOTA", "NO_EXISTE",
                    new AutorizacionRemotaException("La autorización no existe."));
        }
        if (!autorizacion.isActiva()) {
            rechazar(admin, "DESACTIVACION_AUTORIZACION_REMOTA", "SIN_CAMBIO",
                    new AutorizacionRemotaException("La autorización ya está inactiva."));
        }

        autorizacionRepository.desactivar(idAutorizacion);
        logRepository.registrarEvento(admin, "DESACTIVACION_AUTORIZACION_REMOTA",
                LogAuditoria.Resultado.AUTORIZADO,
                "Autorización: " + idAutorizacion, null);
    }

    private void verificarAdmin(Usuario admin, String evento)
            throws SQLException, AutorizacionRemotaException {
        Usuario actual = usuarioRepository.buscarPorId(admin.getIdUsuario());
        Permiso permiso = permisoRepository.buscarPorNombre(PERMISO_REMOTO);
        if (actual == null || permiso == null || !actual.tienePermiso(permiso)) {
            rechazar(admin, evento, "SIN_PERMISOS_REMOTO",
                    new AutorizacionRemotaException("No posee permisos para autorizar trabajo remoto."));
        }
    }

    private void rechazar(Usuario admin, String evento, String motivo, AutorizacionRemotaException ex)
            throws SQLException, AutorizacionRemotaException {
        logRepository.registrarRechazo(admin, evento, motivo, ex.getMessage(), null);
        throw ex;
    }
}