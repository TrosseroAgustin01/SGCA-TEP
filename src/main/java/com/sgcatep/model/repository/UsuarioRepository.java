package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Rol;
import com.sgcatep.model.domain.Usuario;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UsuarioRepository {

    private final AreaRepository areaRepository = new AreaRepository();
    private final RolRepository rolRepository = new RolRepository();

    public Usuario buscarPorId(int idUsuario) throws SQLException {
        String sql = "SELECT * FROM USUARIO WHERE id_usuario = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    cargarRoles(usuario);
                    return usuario;
                }
            }
        }
        return null;
    }

    public Usuario buscarPorDni(String dni) throws SQLException {
        String sql = "SELECT * FROM USUARIO WHERE dni = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    cargarRoles(usuario);
                    return usuario;
                }
            }
        }
        return null;
    }

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT * FROM USUARIO WHERE email = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    cargarRoles(usuario);
                    return usuario;
                }
            }
        }
        return null;
    }

    public Usuario autenticar(String dni, String contraseñaPlana) throws SQLException {
        String contraseñaHash = hashearContraseña(contraseñaPlana);
        String sql = "SELECT * FROM USUARIO WHERE dni = ? AND contraseña = ? AND activo = TRUE";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dni);
            stmt.setString(2, contraseñaHash);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    cargarRoles(usuario);
                    return usuario;
                }
            }
        }
        return null;
    }

    public List<Usuario> listarTodos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM USUARIO ORDER BY apellido, nombre";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = mapearUsuario(rs);
                cargarRoles(usuario);
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    public List<Usuario> listarActivos() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM USUARIO WHERE activo = TRUE ORDER BY apellido, nombre";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Usuario usuario = mapearUsuario(rs);
                cargarRoles(usuario);
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    public List<Usuario> listarPorArea(int idArea) throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM USUARIO WHERE id_area = ? ORDER BY apellido, nombre";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idArea);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Usuario usuario = mapearUsuario(rs);
                    cargarRoles(usuario);
                    usuarios.add(usuario);
                }
            }
        }
        return usuarios;
    }

    public int insertar(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO USUARIO (dni, nombre, apellido, email, contraseña, activo, id_area) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getDni());
            stmt.setString(2, usuario.getNombre());
            stmt.setString(3, usuario.getApellido());
            stmt.setString(4, usuario.getEmail());
            stmt.setString(5, hashearContraseña(usuario.getContraseña()));
            stmt.setBoolean(6, usuario.isActivo());
            stmt.setInt(7, usuario.getArea().getIdArea());

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el usuario");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    usuario.setIdUsuario(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public boolean actualizar(Usuario usuario) throws SQLException {
        String sql = "UPDATE USUARIO SET nombre = ?, apellido = ?, email = ?, " +
                "activo = ?, id_area = ? WHERE id_usuario = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getApellido());
            stmt.setString(3, usuario.getEmail());
            stmt.setBoolean(4, usuario.isActivo());
            stmt.setInt(5, usuario.getArea().getIdArea());
            stmt.setInt(6, usuario.getIdUsuario());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean cambiarContraseña(int idUsuario, String nuevaContraseña) throws SQLException {
        String sql = "UPDATE USUARIO SET contraseña = ? WHERE id_usuario = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, hashearContraseña(nuevaContraseña));
            stmt.setInt(2, idUsuario);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int idUsuario) throws SQLException {
        String sql = "UPDATE USUARIO SET activo = FALSE WHERE id_usuario = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean activar(int idUsuario) throws SQLException {
        String sql = "UPDATE USUARIO SET activo = TRUE WHERE id_usuario = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    public boolean asignarRol(int idUsuario, int idArea, int idRol) throws SQLException {
        String sql = "INSERT INTO USUARIO_ROL (id_usuario, id_area, id_rol) VALUES (?, ?, ?)";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idArea);
            stmt.setInt(3, idRol);

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean quitarRol(int idUsuario, int idArea, int idRol) throws SQLException {
        String sql = "DELETE FROM USUARIO_ROL WHERE id_usuario = ? AND id_area = ? AND id_rol = ?";
        Connection conn = ConexionDB.getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            stmt.setInt(2, idArea);
            stmt.setInt(3, idRol);

            return stmt.executeUpdate() > 0;
        }
    }

    private void cargarRoles(Usuario usuario) throws SQLException {
        List<Rol> roles = rolRepository.listarRolesDeUsuario(usuario.getIdUsuario());
        usuario.setRoles(roles);
    }

    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        int idArea = rs.getInt("id_area");
        Area area = areaRepository.buscarPorId(idArea);

        return new Usuario(
                rs.getInt("id_usuario"),
                rs.getString("dni"),
                rs.getString("nombre"),
                rs.getString("apellido"),
                rs.getString("email"),
                rs.getString("contraseña"),
                rs.getBoolean("activo"),
                area,
                rs.getTimestamp("fecha_creacion").toLocalDateTime()
        );
    }

    private String hashearContraseña(String contraseña) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(contraseña.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Error hasheando contraseña", e);
        }
    }
}