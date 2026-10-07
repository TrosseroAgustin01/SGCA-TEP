package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.Permiso;
import com.sgcatep.model.domain.Sistema;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PermisoRepository {

    private final SistemaRepository sistemaRepository = new SistemaRepository();

    public Permiso buscarPorId(int idPermiso) throws SQLException {
        String sql = "SELECT * FROM PERMISO WHERE id_permiso = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPermiso);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Permiso permiso = mapearPermiso(rs);
                    cargarSistemas(permiso);
                    return permiso;
                }
            }
        }
        return null;
    }

    public Permiso buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM PERMISO WHERE nombre_permiso = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Permiso permiso = mapearPermiso(rs);
                    cargarSistemas(permiso);
                    return permiso;
                }
            }
        }
        return null;
    }

    public List<Permiso> listarTodos() throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String sql = "SELECT * FROM PERMISO ORDER BY nombre_permiso";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Permiso permiso = mapearPermiso(rs);
                cargarSistemas(permiso);
                permisos.add(permiso);
            }
        }
        return permisos;
    }

    public List<Permiso> listarActivos() throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String sql = "SELECT * FROM PERMISO WHERE activo = TRUE ORDER BY nombre_permiso";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Permiso permiso = mapearPermiso(rs);
                cargarSistemas(permiso);
                permisos.add(permiso);
            }
        }
        return permisos;
    }

    public List<Permiso> listarPermisosDeRol(int idRol) throws SQLException {
        List<Permiso> permisos = new ArrayList<>();
        String sql = "SELECT p.* FROM PERMISO p " +
                "JOIN ROL_PERMISO rp ON p.id_permiso = rp.id_permiso " +
                "WHERE rp.id_rol = ? AND p.activo = TRUE " +
                "ORDER BY p.nombre_permiso";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idRol);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Permiso permiso = mapearPermiso(rs);
                    cargarSistemas(permiso);
                    permisos.add(permiso);
                }
            }
        }
        return permisos;
    }

    public int insertar(Permiso permiso) throws SQLException {
        String sql = "INSERT INTO PERMISO (nombre_permiso, descripcion, activo) VALUES (?, ?, ?)";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, permiso.getNombre());
            stmt.setString(2, permiso.getDescripcion());
            stmt.setBoolean(3, true);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el permiso");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    permiso.setIdPermiso(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public boolean actualizar(Permiso permiso) throws SQLException {
        String sql = "UPDATE PERMISO SET nombre_permiso = ?, descripcion = ? WHERE id_permiso = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, permiso.getNombre());
            stmt.setString(2, permiso.getDescripcion());
            stmt.setInt(3, permiso.getIdPermiso());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int idPermiso) throws SQLException {
        String sql = "UPDATE PERMISO SET activo = FALSE WHERE id_permiso = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPermiso);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean asociarSistema(int idPermiso, int idSistema) throws SQLException {
        String sql = "INSERT INTO PERMISO_SISTEMA (id_permiso, id_sistema, obligatorio) VALUES (?, ?, ?)";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPermiso);
            stmt.setInt(2, idSistema);
            stmt.setBoolean(3, true);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desasociarSistema(int idPermiso, int idSistema) throws SQLException {
        String sql = "DELETE FROM PERMISO_SISTEMA WHERE id_permiso = ? AND id_sistema = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idPermiso);
            stmt.setInt(2, idSistema);

            return stmt.executeUpdate() > 0;
        }
    }

    private void cargarSistemas(Permiso permiso) throws SQLException {
        List<Sistema> sistemas = new ArrayList<>();
        String sql = "SELECT s.* FROM SISTEMA s " +
                "JOIN PERMISO_SISTEMA ps ON s.id_sistema = ps.id_sistema " +
                "WHERE ps.id_permiso = ? AND s.activo = TRUE";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, permiso.getIdPermiso());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sistema sistema = new Sistema(
                            rs.getInt("id_sistema"),
                            rs.getString("nombre_sistema"),
                            rs.getString("url_endpoint"),
                            rs.getString("descripcion")
                    );
                    sistemas.add(sistema);
                }
            }
        }
        permiso.setSistemas(sistemas);
    }

    private Permiso mapearPermiso(ResultSet rs) throws SQLException {
        return new Permiso(
                rs.getInt("id_permiso"),
                rs.getString("nombre_permiso"),
                rs.getString("descripcion")
        );
    }
}