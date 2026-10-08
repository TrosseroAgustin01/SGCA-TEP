package com.sgcatep.model.service;

import com.sgcatep.model.domain.AutorizacionRemota;
import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AutorizacionRemotaRepository;
import com.sgcatep.model.repository.FichadaRepository;

import java.sql.SQLException;

public class Validador {

    private final FichadaRepository fichadaRepository = new FichadaRepository();
    private final AutorizacionRemotaRepository autorizacionRepository = new AutorizacionRemotaRepository();

    public boolean verificarEstadoUsuario(Usuario usuario) {
        if (usuario == null) {
            return false;
        }
        return usuario.isActivo();
    }

    public boolean verificarPermisos(Usuario usuario, Sistema sistema) {
        if (usuario == null || sistema == null) {
            return false;
        }
        return usuario.tienePermisoEnSistema(sistema);
    }

    public boolean verificarPresencia(Usuario usuario) throws SQLException {
        if (usuario == null) {
            return false;
        }
        return fichadaRepository.tieneEntradaHoy(usuario.getIdUsuario());
    }

    public boolean verificarAutorizacionRemota(Usuario usuario) throws SQLException {
        if (usuario == null) {
            return false;
        }
        AutorizacionRemota autorizacion = autorizacionRepository.obtenerVigenteDeUsuario(usuario.getIdUsuario());
        return autorizacion != null && autorizacion.estaVigente();
    }

    public boolean validarDatos(Usuario usuario, String tipo, String modalidad) {
        if (usuario == null) {
            return false;
        }

        if (tipo == null || tipo.trim().isEmpty()) {
            return false;
        }

        if (!tipo.equals("ENTRADA") && !tipo.equals("SALIDA")) {
            return false;
        }

        if (modalidad == null || modalidad.trim().isEmpty()) {
            return false;
        }

        if (!modalidad.equals("PRESENCIAL") && !modalidad.equals("REMOTA")) {
            return false;
        }

        return true;
    }

    public boolean validarDni(String dni) {
        if (dni == null || dni.trim().isEmpty()) {
            return false;
        }
        return dni.matches("\\d{7,10}");
    }

    public boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    public boolean validarContraseña(String contraseña) {
        if (contraseña == null || contraseña.trim().isEmpty()) {
            return false;
        }
        return contraseña.length() >= 4;
    }
}