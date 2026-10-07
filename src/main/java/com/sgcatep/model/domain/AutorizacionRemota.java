package com.sgcatep.model.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AutorizacionRemota {

    private int idAutorizacion;
    private Usuario usuario;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String motivo;
    private boolean activa;
    private Usuario creadaPor;
    private LocalDateTime fechaCreacion;

    public AutorizacionRemota(Usuario usuario, LocalDate fechaInicio, LocalDate fechaFin,
                              String motivo, Usuario creadaPor) {
        this.usuario = usuario;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
        this.activa = true;
        this.creadaPor = creadaPor;
    }

    public AutorizacionRemota(int idAutorizacion, Usuario usuario, LocalDate fechaInicio,
                              LocalDate fechaFin, String motivo, boolean activa,
                              Usuario creadaPor, LocalDateTime fechaCreacion) {
        this.idAutorizacion = idAutorizacion;
        this.usuario = usuario;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.motivo = motivo;
        this.activa = activa;
        this.creadaPor = creadaPor;
        this.fechaCreacion = fechaCreacion;
    }

    public int getIdAutorizacion() {
        return idAutorizacion;
    }

    public void setIdAutorizacion(int idAutorizacion) {
        this.idAutorizacion = idAutorizacion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public Usuario getCreadaPor() {
        return creadaPor;
    }

    public void setCreadaPor(Usuario creadaPor) {
        this.creadaPor = creadaPor;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean estaVigente() {
        if (!activa) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        return !hoy.isBefore(fechaInicio) && !hoy.isAfter(fechaFin);
    }

    @Override
    public String toString() {
        return "AutorizacionRemota{" +
                "idAutorizacion=" + idAutorizacion +
                ", usuario=" + (usuario != null ? usuario.getDni() : "null") +
                ", fechaInicio=" + fechaInicio +
                ", fechaFin=" + fechaFin +
                ", activa=" + activa +
                '}';
    }
}