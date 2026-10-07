package com.sgcatep.model.domain;

import java.time.LocalDateTime;

public class Fichada {

    public enum Tipo { ENTRADA, SALIDA }
    public enum Modalidad { PRESENCIAL, REMOTA }

    private int idFichada;
    private Usuario usuario;
    private Tipo tipo;
    private Modalidad modalidad;
    private LocalDateTime fechaHora;
    private AutorizacionRemota autorizacionRemota;
    private String observaciones;

    public Fichada(Usuario usuario, Tipo tipo, Modalidad modalidad) {
        this.usuario = usuario;
        this.tipo = tipo;
        this.modalidad = modalidad;
        this.fechaHora = LocalDateTime.now();
    }

    public Fichada(int idFichada, Usuario usuario, Tipo tipo, Modalidad modalidad,
                   LocalDateTime fechaHora, AutorizacionRemota autorizacionRemota,
                   String observaciones) {
        this.idFichada = idFichada;
        this.usuario = usuario;
        this.tipo = tipo;
        this.modalidad = modalidad;
        this.fechaHora = fechaHora;
        this.autorizacionRemota = autorizacionRemota;
        this.observaciones = observaciones;
    }

    public int getIdFichada() {
        return idFichada;
    }

    public void setIdFichada(int idFichada) {
        this.idFichada = idFichada;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public Modalidad getModalidad() {
        return modalidad;
    }

    public void setModalidad(Modalidad modalidad) {
        this.modalidad = modalidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public AutorizacionRemota getAutorizacionRemota() {
        return autorizacionRemota;
    }

    public void setAutorizacionRemota(AutorizacionRemota autorizacionRemota) {
        this.autorizacionRemota = autorizacionRemota;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean esVigente() {
        if (modalidad == Modalidad.PRESENCIAL) {
            return true;
        }
        return autorizacionRemota != null && autorizacionRemota.estaVigente();
    }

    public boolean esTipoEntrada() {
        return tipo == Tipo.ENTRADA;
    }

    @Override
    public String toString() {
        return "Fichada{" +
                "idFichada=" + idFichada +
                ", usuario=" + (usuario != null ? usuario.getDni() : "null") +
                ", tipo=" + tipo +
                ", modalidad=" + modalidad +
                ", fechaHora=" + fechaHora +
                '}';
    }
}