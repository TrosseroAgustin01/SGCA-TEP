package com.sgcatep.controller;

import com.sgcatep.exception.CambioClaveObligatorioException;
import com.sgcatep.exception.UsuarioInactivoException;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.GestorAcceso;

import java.sql.SQLException;

public class LoginController {

    public record Resultado(Usuario usuario, boolean cambioPendiente, String mensaje) {}

    private final GestorAcceso gestorAcceso = new GestorAcceso();

    public Resultado login(String dni, String contrasena) {
        if (dni.isEmpty() || contrasena.isEmpty()) {
            return new Resultado(null, false, "Debe completar todos los campos.");
        }
        try {
            Usuario usuario = gestorAcceso.login(dni, contrasena);
            if (usuario == null) {
                return new Resultado(null, false, "DNI o contraseña incorrectos.");
            }
            return new Resultado(usuario, false, "");
        } catch (CambioClaveObligatorioException ex) {
            return new Resultado(null, true, ex.getMessage());
        } catch (UsuarioInactivoException ex) {
            return new Resultado(null, false, ex.getMessage());
        } catch (SQLException ex) {
            return new Resultado(null, false, "Error de conexión: " + ex.getMessage());
        }
    }
}