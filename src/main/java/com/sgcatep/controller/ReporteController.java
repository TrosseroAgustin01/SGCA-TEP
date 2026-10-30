package com.sgcatep.controller;

import com.sgcatep.exception.ReporteException;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.GestorReporte;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReporteController {

    public record Resultado(boolean ok, String mensaje) {}

    private final GestorReporte gestor = new GestorReporte();

    public Resultado generar(Usuario usuario, String tipo, LocalDate desde, LocalDate hasta, File carpeta) {
        try {
            GestorReporte.Resultado r = gestor.generar(usuario, tipo, desde, hasta, carpeta);
            return new Resultado(true, "✓ Reporte generado en " + r.ruta() + " (" + r.filas() + " filas).");
        } catch (ReporteException ex) {
            return new Resultado(false, "✗ " + ex.getMessage());
        } catch (SQLException ex) {
            return new Resultado(false, "Error de base de datos: " + ex.getMessage());
        }
    }
}