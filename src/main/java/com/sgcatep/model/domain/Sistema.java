package com.sgcatep.model.domain;

import com.sgcatep.model.domain.Usuario;

public class Sistema {

    private int idSistema;
    private String nombre;
    private String urlEndpoint;
    private String descripcion;

    public Sistema(String nombre, String urlEndpoint, String descripcion) {
        this.nombre = nombre;
        this.urlEndpoint = urlEndpoint;
        this.descripcion = descripcion;
    }

    public Sistema(int idSistema, String nombre, String urlEndpoint, String descripcion) {
        this.idSistema = idSistema;
        this.nombre = nombre;
        this.urlEndpoint = urlEndpoint;
        this.descripcion = descripcion;
    }

    public int getIdSistema() {
        return idSistema;
    }

    public void setIdSistema(int idSistema) {
        this.idSistema = idSistema;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUrlEndpoint() {
        return urlEndpoint;
    }

    public void setUrlEndpoint(String urlEndpoint) {
        this.urlEndpoint = urlEndpoint;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean validarAcceso(Usuario usuario) {
        if (usuario == null || !usuario.isActivo()) {
            return false;
        }
        return usuario.tienePermisoEnSistema(this);
    }

    public void registrarIntento(Usuario usuario, String resultado) {
        // La lógica real va en LogAuditoriaRepository
        // Este método queda como "fachada" del diagrama de clases
        System.out.println("Intento registrado: " + usuario.getDni() + " → " + resultado);
    }

    @Override
    public String toString() {
        return "Sistema{" +
                "idSistema=" + idSistema +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}