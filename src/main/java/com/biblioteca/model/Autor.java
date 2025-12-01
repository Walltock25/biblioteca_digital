package com.biblioteca.model;

import java.util.Objects;

/**
 * Entidad que representa un autor de libros.
 * Mapea la tabla 'Autores' en FNBC.
 */
public class Autor {

    private Integer idAutor;
    private String nombre;
    private String nacionalidad;

    // Constructores
    public Autor() {}

    public Autor(String nombre) {
        this.nombre = nombre;
    }

    public Autor(String nombre, String nacionalidad) {
        this.nombre = nombre;
        this.nacionalidad = nacionalidad;
    }

    // Getters y Setters
    public Integer getIdAutor() {
        return idAutor;
    }

    public void setIdAutor(Integer idAutor) {
        this.idAutor = idAutor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public void setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Autor autor = (Autor) o;
        return Objects.equals(idAutor, autor.idAutor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idAutor);
    }

    @Override
    public String toString() {
        return nombre;
    }
}