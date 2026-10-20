package com.sgcatep.controller;

import com.sgcatep.model.domain.Sistema;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.SistemaRepository;
import com.sgcatep.model.service.GestorAcceso;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AccesoController {

    public record Resultado(boolean autorizado, String motivo, Sistema sistema) {}

    private final SistemaRepository sistemaRepository = new SistemaRepository();
    private final GestorAcceso gestorAcceso = new GestorAcceso();

    public List<Sistema> listarSistemasActivos() throws SQLException {
        return sistemaRepository.listarActivos();
    }

    public Resultado solicitarAcceso(Usuario usuario, Sistema sistema) throws SQLException {
        Map<String, Object> mapa = gestorAcceso.solicitarAcceso(usuario, sistema);
        return new Resultado((boolean) mapa.get("autorizado"),
                (String) mapa.get("motivo"), sistema);
    }
}