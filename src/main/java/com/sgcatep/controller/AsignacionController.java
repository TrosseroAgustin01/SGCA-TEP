package com.sgcatep.controller;

import com.sgcatep.exception.AsignacionRolException;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AreaRepository;
import com.sgcatep.model.repository.RolRepository;
import com.sgcatep.model.repository.SistemaRepository;
import com.sgcatep.model.repository.UsuarioRepository;
import com.sgcatep.model.service.GestorAcceso;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AsignacionController {

    public record Resultado(boolean ok, String mensaje) {}

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final RolRepository rolRepository = new RolRepository();
    private final AreaRepository areaRepository = new AreaRepository();
    private final SistemaRepository sistemaRepository = new SistemaRepository();
    private final GestorAcceso gestorAcceso = new GestorAcceso();

    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioRepository.listarTodos();
    }

    public List<Area> listarAreas() throws SQLException {
        return areaRepository.listarActivas();
    }

    public List<Rol> listarRoles() throws SQLException {
        return rolRepository.listarActivos();
    }

    public List<String> rolesActuales(int idUsuario) throws SQLException {
        return usuarioRepository.listarRolesConArea(idUsuario);
    }

    public List<String> sistemasAccesibles(int idUsuario) throws SQLException {
        List<String> nombres = new ArrayList<>();
        for (Sistema s : sistemaRepository.listarSistemasDeUsuario(idUsuario)) {
            nombres.add(s.getNombre());
        }
        return nombres;
    }

    // recargo usuario y rol desde BD, como hacía la vista
    public Resultado asignar(Usuario admin, int idUsuario, int idRol, int idArea) {
        try {
            Usuario destino = usuarioRepository.buscarPorId(idUsuario);
            Rol rol = rolRepository.buscarPorId(idRol);
            if (destino == null || rol == null) {
                return new Resultado(false, "✗ No se encontró el usuario o el rol.");
            }
            gestorAcceso.asignarRol(admin, destino, rol, idArea);
            return new Resultado(true, "✓ Rol '" + rol.getNombre()
                    + "' asignado a " + destino.getNombre() + " correctamente.");
        } catch (AsignacionRolException ex) {
            return new Resultado(false, "✗ " + ex.getMessage());
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) {
                return new Resultado(false, "✗ El usuario ya tiene ese rol en esa área.");
            }
            return new Resultado(false, "Error de base de datos: " + ex.getMessage());
        }
    }

    public Resultado quitar(Usuario admin, int idUsuario, int idRol, int idArea) {
        try {
            Usuario destino = usuarioRepository.buscarPorId(idUsuario);
            Rol rol = rolRepository.buscarPorId(idRol);
            if (destino == null || rol == null) {
                return new Resultado(false, "✗ No se encontró el usuario o el rol.");
            }
            gestorAcceso.quitarRol(admin, destino, rol, idArea);
            return new Resultado(true, "✓ Rol '" + rol.getNombre() + "' quitado correctamente.");
        } catch (AsignacionRolException ex) {
            return new Resultado(false, "✗ " + ex.getMessage());
        } catch (SQLException ex) {
            return new Resultado(false, "Error de base de datos: " + ex.getMessage());
        }
    }
}