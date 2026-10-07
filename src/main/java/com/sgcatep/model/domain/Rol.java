package com.sgcatep.model.domain;

import java.util.ArrayList;
import java.util.List;

public class Rol {

    private int idRol;
    private String nombre;
    private String descripcion;
    private List<Permiso> permisos;

    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = new ArrayList<>();
    }

    public Rol(int idRol, String nombre, String descripcion) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = new ArrayList<>();
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
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

    public List<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<Permiso> permisos) {
        this.permisos = permisos;
    }

    public void agregarPermiso(Permiso permiso) {
        if (permiso != null && !permisos.contains(permiso)) {
            permisos.add(permiso);
        }
    }

    public void quitarPermiso(Permiso permiso) {
        permisos.remove(permiso);
    }

    public boolean contiene(Permiso permiso) {
        if (permiso == null) {
            return false;
        }
        return permisos.stream()
                .anyMatch(p -> p.getIdPermiso() == permiso.getIdPermiso());
    }

    public boolean permitirAcceso(Sistema sistema) {
        if (sistema == null) {
            return false;
        }
        return permisos.stream()
                .anyMatch(p -> p.accedeA(sistema));
    }

    @Override
    public String toString() {
        return "Rol{" +
                "idRol=" + idRol +
                ", nombre='" + nombre + '\'' +
                ", permisos=" + permisos.size() +
                '}';
    }
}