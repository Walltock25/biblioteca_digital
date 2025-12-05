package com.biblioteca.model;

import java.util.Objects;

public class Rol {

    private Integer idRol;
    private String nombreRol;
    private String descripcion;

    // Constructores
    public Rol() {}

    public Rol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public Rol(String nombreRol, String descripcion) {
        this.nombreRol = nombreRol;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rol rol = (Rol) o;
        return Objects.equals(nombreRol, rol.nombreRol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombreRol);
    }

    @Override
    public String toString() {
        return nombreRol;
    }
}