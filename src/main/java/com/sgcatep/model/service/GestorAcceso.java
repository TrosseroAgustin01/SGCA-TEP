package com.sgcatep.model.service;

import com.sgcatep.ConexionDB;
import com.sgcatep.exception.AsignacionFallidaException;
import com.sgcatep.exception.AsignacionRolException;
import com.sgcatep.exception.RolInexistenteException;
import com.sgcatep.exception.UsuarioInactivoException;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.LogAuditoriaRepository;
import com.sgcatep.model.repository.RolRepository;
import com.sgcatep.model.repository.UsuarioRepository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GestorAcceso {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final RolRepository rolRepository = new RolRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();
    private final Validador validador = new Validador();

    public Map<String, Object> solicitarAcceso(Usuario usuario, Sistema sistema) throws SQLException {
        Map<String, Object> resultado = new HashMap<>();

        Usuario usuarioActual = usuarioRepository.buscarPorId(usuario.getIdUsuario());
        if (usuarioActual == null) {
            logRepository.registrarRechazo(null, "SOLICITUD_ACCESO",
                    "USUARIO_NO_EXISTE", "Usuario id " + usuario.getIdUsuario() + " no encontrado", sistema);
            resultado.put("autorizado", false);
            resultado.put("motivo", "Usuario no encontrado");
            return resultado;
        }

        if (!validador.verificarEstadoUsuario(usuarioActual)) {
            logRepository.registrarRechazo(usuarioActual, "SOLICITUD_ACCESO",
                    "USUARIO_INACTIVO", "Usuario inactivo", sistema);
            resultado.put("autorizado", false);
            resultado.put("motivo", "Usuario inactivo");
            return resultado;
        }

        if (!validador.verificarPermisos(usuarioActual, sistema)) {
            logRepository.registrarRechazo(usuarioActual, "SOLICITUD_ACCESO",
                    "SIN_PERMISOS", "No posee permisos para este sistema", sistema);
            resultado.put("autorizado", false);
            resultado.put("motivo", "No posee permisos para acceder a este sistema");
            return resultado;
        }

        if (!validador.verificarPresencia(usuarioActual)) {
            logRepository.registrarRechazo(usuarioActual, "SOLICITUD_ACCESO",
                    "SIN_PRESENCIA", "No registra fichada de entrada activa", sistema);
            resultado.put("autorizado", false);
            resultado.put("motivo", "No registra fichada de entrada activa");
            return resultado;
        }

        logRepository.registrarEvento(usuarioActual, "SOLICITUD_ACCESO",
                LogAuditoria.Resultado.AUTORIZADO,
                "Acceso autorizado: Activo, Permisos y Presencia OK", sistema);
        resultado.put("autorizado", true);
        resultado.put("motivo", "Acceso concedido correctamente");
        resultado.put("usuario", usuarioActual);
        resultado.put("sistema", sistema);
        return resultado;
    }

    public Map<String, Object> validarAcceso(Usuario usuario, Sistema sistema) throws SQLException {
        Map<String, Object> resultado = new HashMap<>();

        resultado.put("usuarioActivo", validador.verificarEstadoUsuario(usuario));
        resultado.put("tienePermisos", validador.verificarPermisos(usuario, sistema));
        resultado.put("tienePresencia", validador.verificarPresencia(usuario));
        resultado.put("tieneAutorizacionRemota", validador.verificarAutorizacionRemota(usuario));

        boolean accesoCompleto = (boolean) resultado.get("usuarioActivo")
                && (boolean) resultado.get("tienePermisos")
                && (boolean) resultado.get("tienePresencia");

        resultado.put("accesoCompleto", accesoCompleto);
        return resultado;
    }

    public void asignarRol(Usuario admin, Usuario destino, Rol rol, int idArea)
            throws SQLException, AsignacionRolException {
        if (admin == null || destino == null || rol == null) {
            throw new AsignacionRolException("Datos incompletos para asignar el rol.");
        }

        if (!validador.verificarEstadoUsuario(destino)) {
            logRepository.registrarRechazo(admin, "ASIGNACION_ROL", "USUARIO_INACTIVO",
                    "Usuario destino inactivo: " + destino.getDni(), null);
            throw new UsuarioInactivoException(
                    "El usuario " + destino.getDni() + " está inactivo.");
        }

        if (rolRepository.buscarPorId(rol.getIdRol()) == null) {
            logRepository.registrarRechazo(admin, "ASIGNACION_ROL", "ROL_NO_EXISTE",
                    "Rol inexistente para usuario " + destino.getDni(), null);
            throw new RolInexistenteException("El rol seleccionado no existe.");
        }

        Connection conn = ConexionDB.getConnection();
        conn.setAutoCommit(false);
        try {
            boolean asignado = usuarioRepository.asignarRol(
                    destino.getIdUsuario(), idArea, rol.getIdRol());
            if (!asignado) {
                conn.rollback();
                throw new AsignacionFallidaException("No se pudo registrar la asignación.");
            }

            logRepository.registrarEvento(admin, "ASIGNACION_ROL",
                    LogAuditoria.Resultado.AUTORIZADO,
                    "Destino: " + destino.getIdUsuario() + ", Rol: " + rol.getNombre()
                            + ", Área: " + idArea, null);

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void quitarRol(Usuario admin, Usuario destino, Rol rol, int idArea)
            throws SQLException, AsignacionRolException {
        if (admin == null || destino == null || rol == null) {
            throw new AsignacionRolException("Datos incompletos para quitar el rol.");
        }

        boolean quitado;
        Connection conn = ConexionDB.getConnection();
        conn.setAutoCommit(false);
        try {
            quitado = usuarioRepository.quitarRol(destino.getIdUsuario(), idArea, rol.getIdRol());
            if (quitado) {
                logRepository.registrarEvento(admin, "DESASIGNACION_ROL",
                        LogAuditoria.Resultado.AUTORIZADO,
                        "Destino: " + destino.getIdUsuario() + ", Rol: " + rol.getNombre()
                                + ", Área: " + idArea, null);
                conn.commit();
            } else {
                conn.rollback();
            }
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }

        if (!quitado) {
            logRepository.registrarRechazo(admin, "DESASIGNACION_ROL", "ROL_NO_ASIGNADO",
                    "Destino: " + destino.getIdUsuario() + ", Rol: " + rol.getNombre()
                            + ", Área: " + idArea, null);
            throw new AsignacionRolException("El usuario no tenía ese rol en esa área.");
        }
    }

    public Usuario login(String dni, String contraseña)
            throws SQLException, UsuarioInactivoException {
        if (!validador.validarDni(dni)) {
            logRepository.registrarRechazo(null, "LOGIN",
                    "DNI_INVALIDO", "DNI con formato incorrecto: " + dni, null);
            return null;
        }

        if (!validador.validarContraseña(contraseña)) {
            logRepository.registrarRechazo(null, "LOGIN",
                    "CONTRASEÑA_INVALIDA", "Contraseña vacía o muy corta", null);
            return null;
        }

        Usuario usuario = usuarioRepository.autenticar(dni, contraseña);

        if (usuario == null) {
            logRepository.registrarRechazo(null, "LOGIN",
                    "CREDENCIALES_INCORRECTAS", "DNI o contraseña incorrectos: " + dni, null);
            return null;
        }

        if (!usuario.isActivo()) {
            logRepository.registrarRechazo(usuario, "LOGIN",
                    "USUARIO_INACTIVO", "Usuario autenticado pero inactivo", null);
            throw new UsuarioInactivoException(
                    "La cuenta está inactiva. Consulte con el administrador.");
        }

        logRepository.registrarEvento(usuario, "LOGIN",
                LogAuditoria.Resultado.AUTORIZADO,
                "Login exitoso", null);

        return usuario;
    }
}