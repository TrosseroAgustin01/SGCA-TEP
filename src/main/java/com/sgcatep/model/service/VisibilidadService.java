package com.sgcatep.model.service;

import com.sgcatep.model.domain.Area;
import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.AreaRepository;
import com.sgcatep.model.repository.FichadaRepository;
import com.sgcatep.model.repository.LogAuditoriaRepository;
import com.sgcatep.model.repository.UsuarioRepository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class VisibilidadService {

    public enum Alcance { PROPIO, AREA, TODAS }

    public record Visibilidad(Alcance alcance, List<Fichada> fichadas) {}
    public record PaginaLogs(Alcance alcance, int total, List<LogAuditoria> registros) {}

    private static final String VER_FICHADAS = "Ver fichadas";
    private static final String GENERAR_REPORTES = "Generar reportes";
    private static final String CODIGO_RH = "RH";

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final AreaRepository areaRepository = new AreaRepository();
    private final FichadaRepository fichadaRepository = new FichadaRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();

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

    public PaginaLogs logsVisibles(Usuario usuario, Integer idArea, int pagina, int tamanio)
            throws SQLException {
        int id = usuario.getIdUsuario();
        List<Integer> areas = usuarioRepository.listarAreasConPermisos(
                id, VER_FICHADAS, GENERAR_REPORTES);
        List<Object> params = new ArrayList<>();
        Alcance alcance;
        String condicion;

        Area rh = areaRepository.buscarPorCodigo(CODIGO_RH);
        if (areas.isEmpty()) {
            alcance = Alcance.PROPIO;
            condicion = "l.id_usuario = ?";
            params.add(id);
        } else if (rh != null && areas.contains(rh.getIdArea())) {
            alcance = Alcance.TODAS;
            condicion = "1 = 1";
        } else {
            alcance = Alcance.AREA;
            condicion = "l.id_usuario IN (SELECT ur.id_usuario FROM USUARIO_ROL ur"
                    + " WHERE ur.id_area IN (" + marcas(areas.size()) + "))";
            params.addAll(areas);
        }

        if (idArea != null) {
            condicion += " AND l.id_usuario IN (SELECT u.id_usuario FROM USUARIO u WHERE u.id_area = ?)";
            params.add(idArea);
        }

        int total = logRepository.contarVisibles(condicion, params);
        List<LogAuditoria> registros = logRepository.listarVisibles(
                condicion, params, pagina * tamanio, tamanio);
        return new PaginaLogs(alcance, total, registros);
    }

    private String marcas(int n) {
        return String.join(",", Collections.nCopies(n, "?"));
    }
}