package com.sgcatep.model.service;

import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AreaRepository;
import com.sgcatep.model.repository.FichadaRepository;
import com.sgcatep.model.repository.UsuarioRepository;

import java.sql.SQLException;
import java.util.List;

public class VisibilidadService {

    public enum Alcance { PROPIO, AREA, TODAS }

    public record Visibilidad(Alcance alcance, List<Fichada> fichadas) {}

    private static final String VER_FICHADAS = "Ver fichadas";
    private static final String GENERAR_REPORTES = "Generar reportes";
    private static final String CODIGO_RH = "RH";

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final AreaRepository areaRepository = new AreaRepository();
    private final FichadaRepository fichadaRepository = new FichadaRepository();

    public Visibilidad fichadasVisibles(Usuario usuario) throws SQLException {
        int id = usuario.getIdUsuario();
        List<Integer> areas = usuarioRepository.listarAreasConPermisos(
                id, VER_FICHADAS, GENERAR_REPORTES);

        if (areas.isEmpty()) {
            return new Visibilidad(Alcance.PROPIO, fichadaRepository.listarPorUsuario(id));
        }

        Area rh = areaRepository.buscarPorCodigo(CODIGO_RH);
        if (rh != null && areas.contains(rh.getIdArea())) {
            return new Visibilidad(Alcance.TODAS, fichadaRepository.listarTodas());
        }
        return new Visibilidad(Alcance.AREA, fichadaRepository.listarPorAreas(areas));
    }
}