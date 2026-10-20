package com.sgcatep.controller;

import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.service.GestorFichada;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class FichadaController {

    public record Resultado(boolean registrada, String mensaje) {}

    private final GestorFichada gestorFichada = new GestorFichada();

    // los combos mandan texto, acá se convierte a enum
    public Resultado registrar(Usuario usuario, String tipoTexto, String modalidadTexto)
            throws SQLException {
        Fichada.Tipo tipo = Fichada.Tipo.valueOf(tipoTexto);
        Fichada.Modalidad modalidad = Fichada.Modalidad.valueOf(modalidadTexto);

        Fichada fichada = gestorFichada.registrarFichada(usuario, tipo, modalidad);

        if (fichada != null) {
            String hora = fichada.getFechaHora().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            return new Resultado(true, "✓ Fichada registrada — Tipo: " + tipo
                    + " | Modalidad: " + modalidad + " | Hora: " + hora);
        }
        return new Resultado(false, "✗ " + (modalidad == Fichada.Modalidad.REMOTA
                ? "No posee autorización remota vigente."
                : "No se pudo registrar la fichada."));
    }
}