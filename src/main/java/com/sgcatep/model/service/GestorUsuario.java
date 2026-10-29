package com.sgcatep.model.service;

import com.sgcatep.exception.AutoDesactivacionException;
import com.sgcatep.exception.DatoDuplicadoException;
import com.sgcatep.exception.GestionUsuarioException;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Permiso;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.LogAuditoriaRepository;
import com.sgcatep.model.repository.PermisoRepository;
import com.sgcatep.model.repository.UsuarioRepository;

import java.sql.SQLException;

public class GestorUsuario {

    private static final String PERMISO_GESTION = "Asignar roles";

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PermisoRepository permisoRepository = new PermisoRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();
    private final Validador validador = new Validador();

    public void alta(Usuario admin, String dni, String nombre, String apellido,
                     String email, String contrasenaInicial, Area area)
            throws SQLException, GestionUsuarioException {
        verificarAdmin(admin, "ALTA_USUARIO");
        validarCampos(admin, "ALTA_USUARIO", dni, nombre, apellido, email, area);

        if (!validador.validarContraseña(contrasenaInicial)) {
            rechazar(admin, "ALTA_USUARIO", "CONTRASENA_INVALIDA",
                    new GestionUsuarioException("La contraseña inicial debe tener al menos 4 caracteres."));
        }
        if (usuarioRepository.buscarPorDni(dni) != null) {
            rechazar(admin, "ALTA_USUARIO", "DNI_DUPLICADO",
                    new DatoDuplicadoException("Ya existe un usuario con ese DNI."));
        }
        if (usuarioRepository.buscarPorEmail(email) != null) {
            rechazar(admin, "ALTA_USUARIO", "EMAIL_DUPLICADO",
                    new DatoDuplicadoException("Ya existe un usuario con ese email."));
        }

        Usuario nuevo = new Usuario(dni, nombre, apellido, email, contrasenaInicial, true, area);
        nuevo.setDebeCambiarClave(true);
        usuarioRepository.insertar(nuevo);
        logRepository.registrarEvento(admin, "ALTA_USUARIO", LogAuditoria.Resultado.AUTORIZADO,
                "Destino: " + nuevo.getIdUsuario() + ", DNI: " + dni, null);
    }

    public void modificar(Usuario admin, int idUsuario, String nombre, String apellido,
                          String email, Area area) throws SQLException, GestionUsuarioException {
        verificarAdmin(admin, "MODIFICACION_USUARIO");

        Usuario destino = usuarioRepository.buscarPorId(idUsuario);
        if (destino == null) {
            rechazar(admin, "MODIFICACION_USUARIO", "USUARIO_NO_EXISTE",
                    new GestionUsuarioException("El usuario no existe."));
        }
        validarCampos(admin, "MODIFICACION_USUARIO", destino.getDni(), nombre, apellido, email, area);

        Usuario porEmail = usuarioRepository.buscarPorEmail(email);
        if (porEmail != null && porEmail.getIdUsuario() != idUsuario) {
            rechazar(admin, "MODIFICACION_USUARIO", "EMAIL_DUPLICADO",
                    new DatoDuplicadoException("Ya existe otro usuario con ese email."));
        }

        destino.setNombre(nombre);
        destino.setApellido(apellido);
        destino.setEmail(email);
        destino.setArea(area);
        usuarioRepository.actualizar(destino);
        logRepository.registrarEvento(admin, "MODIFICACION_USUARIO", LogAuditoria.Resultado.AUTORIZADO,
                "Destino: " + idUsuario, null);
    }

    public void cambiarEstado(Usuario admin, int idUsuario, boolean activo)
            throws SQLException, GestionUsuarioException {
        String evento = activo ? "REACTIVACION_USUARIO" : "DESACTIVACION_USUARIO";
        verificarAdmin(admin, evento);

        if (!activo && admin.getIdUsuario() == idUsuario) {
            rechazar(admin, evento, "AUTO_DESACTIVACION",
                    new AutoDesactivacionException("Un administrador no puede desactivarse a sí mismo."));
        }

        Usuario destino = usuarioRepository.buscarPorId(idUsuario);
        if (destino == null) {
            rechazar(admin, evento, "USUARIO_NO_EXISTE",
                    new GestionUsuarioException("El usuario no existe."));
        }
        if (destino.isActivo() == activo) {
            rechazar(admin, evento, "SIN_CAMBIO",
                    new GestionUsuarioException("La cuenta ya está " + (activo ? "activa." : "inactiva.")));
        }

        if (activo) {
            usuarioRepository.activar(idUsuario);
        } else {
            usuarioRepository.desactivar(idUsuario);
        }
        logRepository.registrarEvento(admin, evento, LogAuditoria.Resultado.AUTORIZADO,
                "Destino: " + idUsuario, null);
    }

    public void cambiarContrasena(Usuario usuario, String actual, String nueva)
            throws SQLException, GestionUsuarioException {
        if (usuarioRepository.autenticar(usuario.getDni(), actual) == null) {
            rechazar(usuario, "CAMBIO_CONTRASENA", "CONTRASENA_ACTUAL_INCORRECTA",
                    new GestionUsuarioException("La contraseña actual no es correcta."));
        }
        if (!validador.validarContraseña(nueva)) {
            rechazar(usuario, "CAMBIO_CONTRASENA", "CONTRASENA_INVALIDA",
                    new GestionUsuarioException("La nueva contraseña debe tener al menos 4 caracteres."));
        }

        usuarioRepository.cambiarContraseña(usuario.getIdUsuario(), nueva);
        logRepository.registrarEvento(usuario, "CAMBIO_CONTRASENA", LogAuditoria.Resultado.AUTORIZADO,
                "Cambio de contraseña propio", null);
    }

    private void verificarAdmin(Usuario admin, String evento)
            throws SQLException, GestionUsuarioException {
        Usuario actual = usuarioRepository.buscarPorId(admin.getIdUsuario());
        Permiso permiso = permisoRepository.buscarPorNombre(PERMISO_GESTION);
        if (actual == null || permiso == null || !actual.tienePermiso(permiso)) {
            rechazar(admin, evento, "SIN_PERMISOS_GESTION",
                    new GestionUsuarioException("No posee permisos para gestionar usuarios."));
        }
    }

    private void validarCampos(Usuario admin, String evento, String dni, String nombre,
                               String apellido, String email, Area area)
            throws SQLException, GestionUsuarioException {
        if (!validador.validarDni(dni) || nombre == null || nombre.isBlank()
                || apellido == null || apellido.isBlank()
                || !validador.validarEmail(email) || area == null) {
            rechazar(admin, evento, "DATOS_INVALIDOS",
                    new GestionUsuarioException("Revise DNI, nombre, apellido, email y área."));
        }
    }

    private void rechazar(Usuario admin, String evento, String motivo, GestionUsuarioException ex)
            throws SQLException, GestionUsuarioException {
        logRepository.registrarRechazo(admin, evento, motivo, ex.getMessage(), null);
        throw ex;
    }
}