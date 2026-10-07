package com.sgcatep.model.domain;

import java.util.ArrayList;
import java.util.List;

public class Permiso {

    private int idPermiso;
    private String nombre;
    private String descripcion;
    private List<Sistema> sistemas;

    public Permiso(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sistemas = new ArrayList<>();
    }

    public Permiso(int idPermiso, String nombre, String descripcion) {
        this.idPermiso = idPermiso;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.sistemas = new ArrayList<>();
    }

    public int getIdPermiso() {
        return idPermiso;
    }

    public void setIdPermiso(int idPermiso) {
        this.idPermiso = idPermiso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<Sistema> getSistemas() {
        return sistemas;
    }

    public void setSistemas(List<Sistema> sistemas) {
        this.sistemas = sistemas;
    }

    public void asociarSistema(Sistema sistema) {
        if (sistema != null && !sistemas.contains(sistema)) {
            sistemas.add(sistema);
        }
    }

    public void desasociarSistema(Sistema sistema) {
        sistemas.remove(sistema);
    }

    public boolean accedeA(Sistema sistema) {
        if (sistema == null) {
            return false;
        }
        return sistemas.stream()
                .anyMatch(s -> s.getIdSistema() == sistema.getIdSistema());
    }

    @Override
    public String toString() {
        return "Permiso{" +
                "idPermiso=" + idPermiso +
                ", nombre='" + nombre + '\'' +
                '}';
    }
}