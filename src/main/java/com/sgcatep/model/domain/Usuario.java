package com.sgcatep.model.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Usuario {

    private int idUsuario;
    private String dni;
    private String nombre;
    private String apellido;
    private String email;
    private String contraseña;
    private boolean activo;
    private Area area;
    private List<Rol> roles;
    private LocalDateTime fechaCreacion;

    public Usuario(String dni, String nombre, String apellido, String email,
                   String contraseña, boolean activo, Area area) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contraseña = contraseña;
        this.activo = activo;
        this.area = area;
        this.roles = new ArrayList<>();
    }

    public Usuario(int idUsuario, String dni, String nombre, String apellido, String email,
                   String contraseña, boolean activo, Area area, LocalDateTime fechaCreacion) {
        this.idUsuario = idUsuario;
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.contraseña = contraseña;
        this.activo = activo;
        this.area = area;
        this.fechaCreacion = fechaCreacion;
        this.roles = new ArrayList<>();
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public List<Rol> getRoles() {
        return roles;
    }

    public void setRoles(List<Rol> roles) {
        this.roles = roles;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void asignarRol(Rol rol) {
        if (rol != null && !roles.contains(rol)) {
            roles.add(rol);
        }
    }

    public void quitarRol(Rol rol) {
        roles.remove(rol);
    }

    public boolean tienePermiso(Permiso permiso) {
        if (permiso == null) {
            return false;
        }
        return roles.stream().anyMatch(rol -> rol.contiene(permiso));
    }

    public boolean tienePermisoEnSistema(Sistema sistema) {
        if (sistema == null) {
            return false;
        }
        return roles.stream().anyMatch(rol -> rol.permitirAcceso(sistema));
    }

    public List<Permiso> getPermisos() {
        List<Permiso> todosLosPermisos = new ArrayList<>();
        for (Rol rol : roles) {
            for (Permiso permiso : rol.getPermisos()) {
                if (!todosLosPermisos.contains(permiso)) {
                    todosLosPermisos.add(permiso);
                }
            }
        }
        return todosLosPermisos;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", dni='" + dni + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", activo=" + activo +
                ", roles=" + roles.size() +
                '}';
    }
}