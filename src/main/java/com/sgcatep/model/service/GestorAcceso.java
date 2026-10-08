package com.sgcatep.model.service;

import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.LogAuditoriaRepository;
import com.sgcatep.model.repository.RolRepository;
import com.sgcatep.model.repository.UsuarioRepository;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GestorAcceso {

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final RolRepository rolRepository = new RolRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();
    private final Validador validador = new Validador();
    private final GestorFichada gestorFichada = new GestorFichada();

    public Map<String, Object> solicitarAcceso(Usuario usuario, Sistema sistema) throws SQLException {
        Map<String, Object> resultado = new HashMap<>();

        // Validación 1: ¿Usuario activo?
        if (!validador.verificarEstadoUsuario(usuario)) {
            logRepository.registrarRechazo(usuario, "SOLICITUD_ACCESO",
                    "USUARIO_INACTIVO", "Usuario inactivo", sistema);

            resultado.put("autorizado", false);
            resultado.put("motivo", "Usuario inactivo");
            return resultado;
        }

        // Validación 2: ¿Tiene permisos?
        if (!validador.verificarPermisos(usuario, sistema)) {
            logRepository.registrarRechazo(usuario, "SOLICITUD_ACCESO",
                    "SIN_PERMISOS", "No posee permisos para este sistema", sistema);

            resultado.put("autorizado", false);
            resultado.put("motivo", "No posee permisos para acceder a este sistema");
            return resultado;
        }

        // Validación 3: ¿Tiene presencia válida?
        if (!validador.verificarPresencia(usuario)) {
            logRepository.registrarRechazo(usuario, "SOLICITUD_ACCESO",
                    "SIN_PRESENCIA", "No registra fichada de entrada activa", sistema);

            resultado.put("autorizado", false);
            resultado.put("motivo", "No registra fichada de entrada activa");
            return resultado;
        }

        // Las 3 validaciones pasaron → AUTORIZADO
        logRepository.registrarEvento(usuario, "SOLICITUD_ACCESO",
                LogAuditoria.Resultado.AUTORIZADO,
                "Acceso autorizado: Activo, Permisos y Presencia OK", sistema);

        resultado.put("autorizado", true);
        resultado.put("motivo", "Acceso concedido correctamente");
        resultado.put("usuario", usuario);
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

    public boolean asignarRol(Usuario usuario, Rol rol, int idArea) throws SQLException {
        if (usuario == null || rol == null) {
            return false;
        }

        if (!validador.verificarEstadoUsuario(usuario)) {
            logRepository.registrarRechazo(usuario, "ASIGNACION_ROL",
                    "USUARIO_INACTIVO", "No se puede asignar rol a usuario inactivo", null);
            return false;
        }

        Rol rolExistente = rolRepository.buscarPorId(rol.getIdRol());
        if (rolExistente == null) {
            logRepository.registrarRechazo(usuario, "ASIGNACION_ROL",
                    "ROL_NO_EXISTE", "El rol seleccionado no existe", null);
            return false;
        }

        boolean asignado = usuarioRepository.asignarRol(
                usuario.getIdUsuario(), idArea, rol.getIdRol());

        if (asignado) {
            String detalle = "Rol " + rol.getNombre() + " asignado en área " + idArea;
            logRepository.registrarEvento(usuario, "ASIGNACION_ROL",
                    LogAuditoria.Resultado.AUTORIZADO, detalle, null);
        }

        return asignado;
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