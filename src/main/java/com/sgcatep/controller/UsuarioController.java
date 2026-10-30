package com.sgcatep.controller;

import com.sgcatep.exception.GestionUsuarioException;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AreaRepository;
import com.sgcatep.model.repository.UsuarioRepository;
import com.sgcatep.model.service.GestorUsuario;

import java.sql.SQLException;
import java.util.List;

public class UsuarioController {

    public record Resultado(boolean ok, String mensaje) {}

    private interface Operacion {
        void ejecutar() throws SQLException, GestionUsuarioException;
    }

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final AreaRepository areaRepository = new AreaRepository();
    private final GestorUsuario gestor = new GestorUsuario();

    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioRepository.listarTodos();
    }

    public List<Area> listarAreas() throws SQLException {
        return areaRepository.listarActivas();
    }

    public Resultado alta(Usuario admin, String dni, String nombre, String apellido,
                          String email, String contrasena, Area area) {
        return correr("Usuario creado correctamente.",
                () -> gestor.alta(admin, dni, nombre, apellido, email, contrasena, area));
    }

    public Resultado modificar(Usuario admin, int id, String nombre, String apellido,
                               String email, Area area) {
        return correr("Datos actualizados.",
                () -> gestor.modificar(admin, id, nombre, apellido, email, area));
    }

    public Resultado cambiarEstado(Usuario admin, int id, boolean activo) {
        return correr(activo ? "Usuario activado." : "Usuario desactivado.",
                () -> gestor.cambiarEstado(admin, id, activo));
    }

    public Resultado cambiarContrasena(Usuario usuario, String actual, String nueva) {
        return correr("Contraseña cambiada correctamente.",
                () -> gestor.cambiarContrasena(usuario, actual, nueva));
    }

    public Resultado cambiarClavePrimerIngreso(String dni, String actual, String nueva) {
        return correr("Contraseña actualizada. Ingrese nuevamente.", () -> {
            Usuario usuario = usuarioRepository.autenticar(dni, actual);
            if (usuario == null) {
                throw new GestionUsuarioException("No se pudo verificar la sesión. Vuelva a ingresar.");
            }
            gestor.cambiarContrasena(usuario, actual, nueva);
        });
    }

    private Resultado correr(String okMensaje, Operacion op) {
        try {
            op.ejecutar();
            return new Resultado(true, "✓ " + okMensaje);
        } catch (GestionUsuarioException ex) {
            return new Resultado(false, "✗ " + ex.getMessage());
        } catch (SQLException ex) {
            return new Resultado(false, ex.getErrorCode() == 1062
                    ? "✗ Ya existe un registro con esos datos."
                    : "Error de base de datos: " + ex.getMessage());
        }
    }
}