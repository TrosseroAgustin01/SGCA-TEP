package com.sgcatep.model.service;

import com.sgcatep.model.domain.AutorizacionRemota;
import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AutorizacionRemotaRepository;
import com.sgcatep.model.repository.FichadaRepository;
import com.sgcatep.model.repository.LogAuditoriaRepository;

import java.sql.SQLException;

public class GestorFichada {

    private final FichadaRepository fichadaRepository = new FichadaRepository();
    private final AutorizacionRemotaRepository autorizacionRepository = new AutorizacionRemotaRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();
    private final Validador validador = new Validador();

    public Fichada registrarFichada(Usuario usuario, Fichada.Tipo tipo, Fichada.Modalidad modalidad)
            throws SQLException {

        if (!validador.verificarEstadoUsuario(usuario)) {
            logRepository.registrarRechazo(usuario, "FICHADA_REGISTRADA",
                    "USUARIO_INACTIVO", "Usuario intentó fichar estando inactivo", null);
            return null;
        }

        if (!validarModalidad(usuario, modalidad)) {
            logRepository.registrarRechazo(usuario, "FICHADA_REGISTRADA",
                    "MODALIDAD_INVALIDA", "No posee autorización remota vigente", null);
            return null;
        }

        Fichada fichada = new Fichada(usuario, tipo, modalidad);

        if (modalidad == Fichada.Modalidad.REMOTA) {
            AutorizacionRemota autorizacion = autorizacionRepository.obtenerVigenteDeUsuario(usuario.getIdUsuario());
            fichada.setAutorizacionRemota(autorizacion);
        }

        int idGenerado = fichadaRepository.insertar(fichada);

        if (idGenerado > 0) {
            String detalle = "Tipo: " + tipo + ", Modalidad: " + modalidad;
            logRepository.registrarEvento(usuario, "FICHADA_REGISTRADA",
                    LogAuditoria.Resultado.AUTORIZADO, detalle, null);
            return fichada;
        }

        return null;
    }

    public Fichada obtenerFichadaVigente(Usuario usuario) throws SQLException {
        if (usuario == null) {
            return null;
        }
        return fichadaRepository.obtenerUltimaFichadaDelDia(usuario.getIdUsuario());
    }

    public boolean validarModalidad(Usuario usuario, Fichada.Modalidad modalidad) throws SQLException {
        if (usuario == null || modalidad == null) {
            return false;
        }

        if (modalidad == Fichada.Modalidad.PRESENCIAL) {
            return true;
        }

        return validador.verificarAutorizacionRemota(usuario);
    }

    public boolean validarPresenciaHoy(Usuario usuario) throws SQLException {
        if (usuario == null) {
            return false;
        }
        return fichadaRepository.tieneEntradaHoy(usuario.getIdUsuario());
    }
}