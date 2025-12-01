package com.biblioteca.model;

import java.util.Objects;

/**
 * Entidad que representa una editorial.
 * Mapea la tabla 'Editoriales' en FNBC.
 */
public class Editorial {

    private Integer idEditorial;
    private String nombre;
    private String pais;
    private String website;

    // Constructores
    public Editorial() {}

    public Editorial(String nombre) {
        this.nombre = nombre;
    }

    public Editorial(String nombre, String pais) {
        this.nombre = nombre;
        this.pais = pais;
    }

    // Getters y Setters
    public Integer getIdEditorial() {
        return idEditorial;
    }

    public void setIdEditorial(Integer idEditorial) {
        this.idEditorial = idEditorial;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Editorial editorial = (Editorial) o;
        return Objects.equals(nombre, editorial.nombre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre);
    }

    @Override
    public String toString() {
        return nombre;
    }
}