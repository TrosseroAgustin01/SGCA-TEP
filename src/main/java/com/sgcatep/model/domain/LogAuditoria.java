package com.sgcatep.model.domain;

import java.time.LocalDateTime;

public class LogAuditoria {

    public enum Resultado { AUTORIZADO, RECHAZADO }

    private int idLog;
    private Usuario usuario;
    private LocalDateTime fechaHora;
    private String tipoEvento;
    private Resultado resultado;
    private String motivoRechazo;
    private String detalle;
    private Sistema sistema;
    private String detallesJson;
    private String direccionIp;

    public LogAuditoria(Usuario usuario, String tipoEvento, Resultado resultado,
                        String detalle, Sistema sistema) {
        this.usuario = usuario;
        this.tipoEvento = tipoEvento;
        this.resultado = resultado;
        this.detalle = detalle;
        this.sistema = sistema;
        this.fechaHora = LocalDateTime.now();
    }

    public LogAuditoria(int idLog, Usuario usuario, LocalDateTime fechaHora, String tipoEvento,
                        Resultado resultado, String motivoRechazo, String detalle,
                        Sistema sistema, String detallesJson, String direccionIp) {
        this.idLog = idLog;
        this.usuario = usuario;
        this.fechaHora = fechaHora;
        this.tipoEvento = tipoEvento;
        this.resultado = resultado;
        this.motivoRechazo = motivoRechazo;
        this.detalle = detalle;
        this.sistema = sistema;
        this.detallesJson = detallesJson;
        this.direccionIp = direccionIp;
    }

    public int getIdLog() {
        return idLog;
    }

    public void setIdLog(int idLog) {
        this.idLog = idLog;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(String tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Resultado getResultado() {
        return resultado;
    }

    public void setResultado(Resultado resultado) {
        this.resultado = resultado;
    }

    public String getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(String motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    public Sistema getSistema() {
        return sistema;
    }

    public void setSistema(Sistema sistema) {
        this.sistema = sistema;
    }

    public String getDetallesJson() {
        return detallesJson;
    }

    public void setDetallesJson(String detallesJson) {
        this.detallesJson = detallesJson;
    }

    public String getDireccionIp() {
        return direccionIp;
    }

    public void setDireccionIp(String direccionIp) {
        this.direccionIp = direccionIp;
    }

    public boolean esAutorizado() {
        return resultado == Resultado.AUTORIZADO;
    }

    public boolean esRechazado() {
        return resultado == Resultado.RECHAZADO;
    }

    @Override
    public String toString() {
        return "LogAuditoria{" +
                "idLog=" + idLog +
                ", usuario=" + (usuario != null ? usuario.getDni() : "null") +
                ", fechaHora=" + fechaHora +
                ", tipoEvento='" + tipoEvento + '\'' +
                ", resultado=" + resultado +
                '}';
    }
}