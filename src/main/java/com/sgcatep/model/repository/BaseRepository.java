package com.sgcatep.model.repository;

import com.sgcatep.ConexionDB;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseRepository {

    protected Connection getConnection() throws SQLException {
        return ConexionDB.getConnection();
    }
}