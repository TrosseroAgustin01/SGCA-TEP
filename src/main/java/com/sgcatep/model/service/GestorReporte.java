package com.sgcatep.model.service;

import com.sgcatep.exception.ReporteException;
import com.sgcatep.model.domain.Fichada;
import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.domain.Permiso;
import com.sgcatep.model.domain.Usuario;
import com.sgcatep.model.repository.LogAuditoriaRepository;
import com.sgcatep.model.repository.PermisoRepository;
import com.sgcatep.model.repository.UsuarioRepository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestorReporte {

    public record Resultado(String ruta, int filas) {}

    private static final String PERMISO_REPORTES = "Generar reportes";
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final UsuarioRepository usuarioRepository = new UsuarioRepository();
    private final PermisoRepository permisoRepository = new PermisoRepository();
    private final LogAuditoriaRepository logRepository = new LogAuditoriaRepository();
    private final VisibilidadService visibilidad = new VisibilidadService();

    public Resultado generar(Usuario usuario, String tipo, LocalDate desde, LocalDate hasta, File carpeta)
            throws SQLException, ReporteException {
        verificarPermiso(usuario);
        validar(usuario, tipo, desde, hasta, carpeta);

        String nombre = "reporte_%s_%s_%s.csv".formatted(
                tipo.toLowerCase(), desde.format(FECHA), hasta.format(FECHA));
        File destino = new File(carpeta, nombre);

        int filas;
        try {
            filas = "FICHADAS".equals(tipo)
                    ? escribirFichadas(usuario, desde, hasta, destino)
                    : escribirAccesos(usuario, desde, hasta, destino);
        } catch (IOException ex) {
            throw registrar(usuario, "ERROR_ESCRITURA",
                    new ReporteException("No se pudo escribir el archivo: " + ex.getMessage()));
        }

        logRepository.registrarEvento(usuario, "REPORTE_GENERADO", LogAuditoria.Resultado.AUTORIZADO,
                "Tipo: " + tipo + ", Archivo: " + nombre + ", Filas: " + filas, null);
        return new Resultado(destino.getAbsolutePath(), filas);
    }

    private void verificarPermiso(Usuario usuario) throws SQLException, ReporteException {
        Usuario actual = usuarioRepository.buscarPorId(usuario.getIdUsuario());
        Permiso permiso = permisoRepository.buscarPorNombre(PERMISO_REPORTES);
        if (actual == null || permiso == null || !actual.tienePermiso(permiso)) {
            throw registrar(usuario, "SIN_PERMISOS_REPORTE",
                    new ReporteException("No posee permisos para generar reportes."));
        }
    }

    private void validar(Usuario usuario, String tipo, LocalDate desde, LocalDate hasta, File carpeta)
            throws SQLException, ReporteException {
        if (!"FICHADAS".equals(tipo) && !"ACCESOS".equals(tipo)) {
            throw registrar(usuario, "TIPO_INVALIDO",
                    new ReporteException("Tipo de reporte inválido."));
        }
        if (desde == null || hasta == null) {
            throw registrar(usuario, "PERIODO_VACIO",
                    new ReporteException("Indique el período completo."));
        }
        if (hasta.isBefore(desde)) {
            throw registrar(usuario, "PERIODO_INVALIDO",
                    new ReporteException("La fecha hasta es anterior a la fecha desde."));
        }
        if (carpeta == null) {
            throw registrar(usuario, "CARPETA_INVALIDA",
                    new ReporteException("Seleccione una carpeta de destino."));
        }
    }

    private int escribirFichadas(Usuario usuario, LocalDate desde, LocalDate hasta, File destino)
            throws SQLException, IOException {
        List<Fichada> lista = visibilidad.fichadasEntre(usuario, desde, hasta);
        try (BufferedWriter w = abrir(destino)) {
            w.write("fecha_hora;apellido;nombre;area;tipo;modalidad;id_autorizacion_remota");
            w.newLine();
            for (Fichada f : lista) {
                w.write(String.join(";",
                        f.getFechaHora().format(FECHA_HORA),
                        limpiar(f.getUsuario().getApellido()),
                        limpiar(f.getUsuario().getNombre()),
                        limpiar(f.getUsuario().getArea().getNombre()),
                        f.getTipo().name(),
                        f.getModalidad().name(),
                        f.getAutorizacionRemota() == null
                                ? "" : String.valueOf(f.getAutorizacionRemota().getIdAutorizacion())));
                w.newLine();
            }
        }
        return lista.size();
    }

    private int escribirAccesos(Usuario usuario, LocalDate desde, LocalDate hasta, File destino)
            throws SQLException, IOException {
        List<LogAuditoria> lista = visibilidad.logsEntre(usuario, desde, hasta);
        try (BufferedWriter w = abrir(destino)) {
            w.write("fecha_hora;tipo_evento;usuario;resultado;motivo_rechazo;detalle");
            w.newLine();
            for (LogAuditoria l : lista) {
                w.write(String.join(";",
                        l.getFechaHora().format(FECHA_HORA),
                        limpiar(l.getTipoEvento()),
                        limpiar(quien(l)),
                        l.getResultado().name(),
                        limpiar(l.getMotivoRechazo()),
                        limpiar(l.getDetalle())));
                w.newLine();
            }
        }
        return lista.size();
    }

    private BufferedWriter abrir(File destino) throws IOException {
        BufferedWriter w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8));
        w.write('\uFEFF');
        return w;
    }

    private String quien(LogAuditoria l) {
        if (l.getUsuario() == null) {
            return "sin usuario";
        }
        return l.getUsuario().getApellido() + ", " + l.getUsuario().getNombre();
    }

    private String limpiar(String s) {
        if (s == null) {
            return "";
        }
        return s.replace(';', ',').replace('\n', ' ').replace('\r', ' ');
    }

    private ReporteException registrar(Usuario usuario, String motivo, ReporteException ex)
            throws SQLException {
        logRepository.registrarRechazo(usuario, "REPORTE_GENERADO", motivo, ex.getMessage(), null);
        return ex;
    }
}