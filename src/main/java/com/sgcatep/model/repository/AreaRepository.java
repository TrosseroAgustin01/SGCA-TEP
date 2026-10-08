package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import com.sgcatep.model.domain.Area;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AreaRepository extends BaseRepository {

    public Area buscarPorId(int idArea) throws SQLException {
        String sql = "SELECT * FROM AREA WHERE id_area = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idArea);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearArea(rs);
                }
            }
        }
        return null;
    }

    public Area buscarPorNombre(String nombre) throws SQLException {
        String sql = "SELECT * FROM AREA WHERE nombre_area = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nombre);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapearArea(rs);
                }
            }
        }
        return null;
    }

    public List<Area> listarTodas() throws SQLException {
        List<Area> areas = new ArrayList<>();
        String sql = "SELECT * FROM AREA ORDER BY nombre_area";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                areas.add(mapearArea(rs));
            }
        }
        return areas;
    }

    public List<Area> listarActivas() throws SQLException {
        List<Area> areas = new ArrayList<>();
        String sql = "SELECT * FROM AREA WHERE activa = TRUE ORDER BY nombre_area";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                areas.add(mapearArea(rs));
            }
        }
        return areas;
    }

    public int insertar(Area area) throws SQLException {
        String sql = "INSERT INTO AREA (nombre_area, codigo_area, descripcion, activa) VALUES (?, ?, ?, ?)";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, area.getNombre());
            stmt.setString(2, area.getCodigo());
            stmt.setString(3, area.getDescripcion());
            stmt.setBoolean(4, true);

            int filasAfectadas = stmt.executeUpdate();
            if (filasAfectadas == 0) {
                throw new SQLException("No se pudo insertar el área");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int idGenerado = generatedKeys.getInt(1);
                    area.setIdArea(idGenerado);
                    return idGenerado;
                }
            }
        }
        return -1;
    }

    public boolean actualizar(Area area) throws SQLException {
        String sql = "UPDATE AREA SET nombre_area = ?, codigo_area = ?, descripcion = ? WHERE id_area = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, area.getNombre());
            stmt.setString(2, area.getCodigo());
            stmt.setString(3, area.getDescripcion());
            stmt.setInt(4, area.getIdArea());

            return stmt.executeUpdate() > 0;
        }
    }

    public boolean desactivar(int idArea) throws SQLException {
        String sql = "UPDATE AREA SET activa = FALSE WHERE id_area = ?";
        Connection conn = getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idArea);
            return stmt.executeUpdate() > 0;
        }
    }

    private Area mapearArea(ResultSet rs) throws SQLException {
        return new Area(
                rs.getInt("id_area"),
                rs.getString("nombre_area"),
                rs.getString("codigo_area"),
                rs.getString("descripcion")
        );
    }
}