package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.Permiso;
import com.sgcatep.model.domain.Rol;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RolRepository extends BaseRepository {

    private final PermisoRepository permisoRepository = new PermisoRepository();

    public Rol buscarPorId(int idRol) throws SQLException {
        String sql = "SELECT * FROM ROL WHERE id_rol = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRol);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rol rol = mapearRol(rs);
                    cargarPermisos(rol);
                    return rol;
                }
            }
        }
        return null;
    }

    public Rol buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM ROL WHERE nombre_rol = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Rol rol = mapearRol(rs);
                    cargarPermisos(rol);
                    return rol;
                }
            }
        }
        return null;
    }

    public List<Rol> listarTodos() throws SQLException {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT * FROM ROL ORDER BY nombre_rol";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Rol rol = mapearRol(rs);
                cargarPermisos(rol);
                roles.add(rol);
            }
        }
        return roles;
    }

    public List<Rol> listarActivos() throws SQLException {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT * FROM ROL WHERE activo = TRUE ORDER BY nombre_rol";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Rol rol = mapearRol(rs);
                cargarPermisos(rol);
                roles.add(rol);
            }
        }
        return roles;
    }

    public List<Rol> listarRolesDeUsuario(int idUsuario) throws SQLException {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT DISTINCT r.* FROM ROL r " +
                "JOIN USUARIO_ROL ur ON r.id_rol = ur.id_rol " +
                "WHERE ur.id_usuario = ? AND r.activo = TRUE " +
                "ORDER BY r.nombre_rol";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Rol rol = mapearRol(rs);
                    cargarPermisos(rol);
                    roles.add(rol);
                }
            }
        }
        return roles;
    }

    public int insertar(Rol rol) throws SQLException {
        String sql = "INSERT INTO ROL (nombre_rol, descripcion, activo) VALUES (?, ?, ?)";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setBoolean(3, true);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el rol");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    rol.setIdRol(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public boolean actualizar(Rol rol) throws SQLException {
        String sql = "UPDATE ROL SET nombre_rol = ?, descripcion = ? WHERE id_rol = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, rol.getNombre());
            stmt.setString(2, rol.getDescripcion());
            stmt.setInt(3, rol.getIdRol());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int idRol) throws SQLException {
        String sql = "UPDATE ROL SET activo = FALSE WHERE id_rol = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRol);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean agregarPermiso(int idRol, int idPermiso) throws SQLException {
        String sql = "INSERT INTO ROL_PERMISO (id_rol, id_permiso) VALUES (?, ?)";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRol);
            stmt.setInt(2, idPermiso);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean quitarPermiso(int idRol, int idPermiso) throws SQLException {
        String sql = "DELETE FROM ROL_PERMISO WHERE id_rol = ? AND id_permiso = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRol);
            stmt.setInt(2, idPermiso);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean quitarTodosLosPermisos(int idRol) throws SQLException {
        String sql = "DELETE FROM ROL_PERMISO WHERE id_rol = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRol);
            return stmt.executeUpdate() >= 0;
        }
    }

    private void cargarPermisos(Rol rol) throws SQLException {
        List<Permiso> permisos = permisoRepository.listarPermisosDeRol(rol.getIdRol());
        rol.setPermisos(permisos);
    }

    private Rol mapearRol(ResultSet rs) throws SQLException {
        return new Rol(
                rs.getInt("id_rol"),
                rs.getString("nombre_rol"),
                rs.getString("descripcion")
        );
    }
}