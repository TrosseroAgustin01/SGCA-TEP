package com.sgcatep.model.service;

import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.LogAuditoriaRepository;
import com.sgcatep.model.repository.RolRepository;
import com.sgcatep.model.repository.UsuarioRepository;
import com.sgcatep.model.domain.Permiso;
import com.sgcatep.ConexionDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class GestorAcceso {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final RolRepository rolRepository = new RolRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();
    private final Validador validador = new Validador();
    private final GestorFichada gestorFichada = new GestorFichada();

    public Map<String, Object> solicitarAcceso(Usuario usuario, Sistema sistema) throws SQLException {
        Map<String, Object> resultado = new HashMap<>();

        // Estado vigente en BD (diagrama CU-05, pasos 4-5 y 14-15).
        // Variable local: cada solicitud tiene su propio estado.
        Usuario usuarioActual = usuarioRepository.buscarPorId(usuario.getIdUsuario());
        if (usuarioActual == null) {
            logRepository.registrarRechazo(null, "SOLICITUD_ACCESO",
                    "USUARIO_NO_EXISTE", "Usuario id " + usuario.getIdUsuario() + " no encontrado", sistema);
            resultado.put("autorizado", false);
            resultado.put("motivo", "Usuario no encontrado");
            return resultado;
        }

        // Validación 1: ¿Usuario activo?
        if (!validador.verificarEstadoUsuario(usuarioActual)) {
            logRepository.registrarRechazo(usuarioActual, "SOLICITUD_ACCESO",
                    "USUARIO_INACTIVO", "Usuario inactivo", sistema);

            resultado.put("autorizado", false);
            resultado.put("motivo", "Usuario inactivo");
            return resultado;
        }

        // Validación 2: ¿Tiene permisos?
        if (!validador.verificarPermisos(usuarioActual, sistema)) {
            logRepository.registrarRechazo(usuarioActual, "SOLICITUD_ACCESO",
                    "SIN_PERMISOS", "No posee permisos para este sistema", sistema);

            resultado.put("autorizado", false);
            resultado.put("motivo", "No posee permisos para acceder a este sistema");
            return resultado;
        }

        // Validación 3: ¿Tiene presencia válida?
        if (!validador.verificarPresencia(usuarioActual)) {
            logRepository.registrarRechazo(usuarioActual, "SOLICITUD_ACCESO",
                    "SIN_PRESENCIA", "No registra fichada de entrada activa", sistema);

            resultado.put("autorizado", false);
            resultado.put("motivo", "No registra fichada de entrada activa");
            return resultado;
        }

        // Las 3 validaciones pasaron → AUTORIZADO
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

    public boolean asignarRol(Usuario admin, Usuario destino, Rol rol, int idArea,
                              List<Permiso> permisos) throws SQLException {
        if (admin == null || destino == null || rol == null) {
            return false;
        }

        if (!validador.verificarEstadoUsuario(destino)) {
            logRepository.registrarRechazo(admin, "ASIGNACION_ROL", "USUARIO_INACTIVO",
                    "Usuario destino inactivo: " + destino.getDni(), null);
            return false;
        }

        if (rolRepository.buscarPorId(rol.getIdRol()) == null) {
            logRepository.registrarRechazo(admin, "ASIGNACION_ROL", "ROL_NO_EXISTE",
                    "Rol inexistente para usuario " + destino.getDni(), null);
            return false;
        }

        // asignamos los roles y permisos
        Connection conn = ConexionDB.getConnection();
        conn.setAutoCommit(false);
        try {
            boolean asignado = usuarioRepository.asignarRol(
                    destino.getIdUsuario(), idArea, rol.getIdRol());
            if (!asignado) {
                conn.rollback();
                return false;
            }

            if (permisos != null) {
                rolRepository.quitarTodosLosPermisos(rol.getIdRol());
                for (Permiso p : permisos) {
                    rolRepository.agregarPermiso(rol.getIdRol(), p.getIdPermiso());
                }
            }

            // El administrador lo registra; el usuario va en el detalle
            logRepository.registrarEvento(admin, "ASIGNACION_ROL",
                    LogAuditoria.Resultado.AUTORIZADO,
                    "Destino: " + destino.getIdUsuario() + ", Rol: " + rol.getNombre()
                            + ", Área: " + idArea, null);

            conn.commit();
            return true;
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public boolean quitarRol(Usuario usuario, Rol rol, int idArea) throws SQLException {
        if (usuario == null || rol == null) {
            return false;
        }

        boolean quitado = usuarioRepository.quitarRol(
                usuario.getIdUsuario(), idArea, rol.getIdRol());

        if (quitado) {
            String detalle = "Rol " + rol.getNombre() + " quitado del área " + idArea;
            logRepository.registrarEvento(usuario, "DESASIGNACION_ROL",
                    LogAuditoria.Resultado.AUTORIZADO, detalle, null);
        }

        return quitado;
    }

    public Usuario login(String dni, String contraseña) throws SQLException {
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
            return null;
        }

        logRepository.registrarEvento(usuario, "LOGIN",
                LogAuditoria.Resultado.AUTORIZADO,
                "Login exitoso", null);

        return usuario;
    }
}