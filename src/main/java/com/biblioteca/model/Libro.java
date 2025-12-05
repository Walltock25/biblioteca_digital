package com.biblioteca.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Libro {

    private Integer idLibro;
    private String isbn;
    private String titulo;
    private Integer anioPublicacion;

    // Relaciones con otras entidades
    private Editorial editorial;
    private Categoria categoria;
    private List<Autor> autores;

    // Constructores
    public Libro() {
        this.autores = new ArrayList<>();
    }

    public Libro(String isbn, String titulo, Integer anioPublicacion) {
        this();
        this.isbn = isbn;
        this.titulo = titulo;
        this.anioPublicacion = anioPublicacion;
    }

    // Getters y Setters
    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public Integer getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(Integer anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public Editorial getEditorial() {
        return editorial;
    }

    public void setEditorial(Editorial editorial) {
        this.editorial = editorial;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public List<Autor> getAutores() {
        return autores;
    }

    public void setAutores(List<Autor> autores) {
        this.autores = autores;
    }

    public void addAutor(Autor autor) {
        if (!this.autores.contains(autor)) {
            this.autores.add(autor);
        }
    }

    // equals y hashCode basados en ISBN (clave natural)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Libro libro = (Libro) o;
        return Objects.equals(isbn, libro.isbn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn);
    }

    @Override
    public String toString() {
        return "Libro{" +
                "idLibro=" + idLibro +
                ", isbn='" + isbn + '\'' +
                ", titulo='" + titulo + '\'' +
                ", anioPublicacion=" + anioPublicacion +
                ", editorial=" + (editorial != null ? editorial.getNombre() : "N/A") +
                ", categoria=" + (categoria != null ? categoria.getNombre() : "N/A") +
                '}';
    }
}