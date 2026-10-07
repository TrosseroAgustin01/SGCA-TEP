package com.sgcatep;

import com.sgcatep.model.domain.LogAuditoria;
import com.sgcatep.model.repository.LogAuditoriaRepository;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            LogAuditoriaRepository repo = new LogAuditoriaRepository();

            System.out.println("=== Todos los logs ===");
            List<LogAuditoria> logs = repo.listarTodos();
            for (LogAuditoria log : logs) {
                String usuario = (log.getUsuario() != null) ? log.getUsuario().getNombre() : "ANÓNIMO";
                String sistema = (log.getSistema() != null) ? log.getSistema().getNombre() : "N/A";
                System.out.println("  → [" + log.getFechaHora() + "] " +
                        usuario + " - " + log.getTipoEvento() +
                        " - " + log.getResultado() +
                        " (Sistema: " + sistema + ")");
            }

            System.out.println("\n=== Logs de Juan García (id=1) ===");
            List<LogAuditoria> logsJuan = repo.listarPorUsuario(1);
            for (LogAuditoria log : logsJuan) {
                System.out.println("  → " + log.getTipoEvento() +
                        " - " + log.getResultado() +
                        " - " + log.getDetalle());
            }

            System.out.println("\n=== Rechazos últimas 24 horas ===");
            List<LogAuditoria> rechazos = repo.listarRechazadosUltimas24Horas();
            System.out.println("Total rechazos: " + rechazos.size());
            for (LogAuditoria log : rechazos) {
                System.out.println("  → " + log.getTipoEvento() +
                        " - Motivo: " + log.getMotivoRechazo());
            }

            ConexionDB.closeConnection();
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}