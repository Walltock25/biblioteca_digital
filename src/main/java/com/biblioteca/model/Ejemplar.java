package com.biblioteca.model;

import com.biblioteca.model.enums.EstadoFisico;
import java.util.Objects;

/**
 * Entidad que representa la COPIA FÍSICA de un libro.
 * Mapea la tabla 'Ejemplares' en FNBC.
 *
 * DISTINCIÓN CRÍTICA:
 * - Un Libro puede tener múltiples Ejemplares (copias físicas)
 * - Cada Ejemplar tiene su propio código de barras, ubicación y estado
 */
public class Ejemplar {

    private Integer idEjemplar;
    private String codigoBarras;
    private EstadoFisico estadoFisico;
    private Boolean disponible;

    // Relaciones
    private Libro libro;
    private Ubicacion ubicacion;

    // Constructores
    public Ejemplar() {
        this.estadoFisico = EstadoFisico.BUENO;
        this.disponible = true;
    }

    public Ejemplar(String codigoBarras, Libro libro) {
        this();
        this.codigoBarras = codigoBarras;
        this.libro = libro;
    }

    // Getters y Setters
    public Integer getIdEjemplar() {
        return idEjemplar;
    }

    public void setIdEjemplar(Integer idEjemplar) {
        this.idEjemplar = idEjemplar;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public EstadoFisico getEstadoFisico() {
        return estadoFisico;
    }

    public void setEstadoFisico(EstadoFisico estadoFisico) {
        this.estadoFisico = estadoFisico;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = ubicacion;
    }

    /**
     * Método de negocio: verifica si el ejemplar puede ser prestado
     */
    public boolean puedeSerPrestado() {
        return disponible &&
                estadoFisico != EstadoFisico.PERDIDO &&
                estadoFisico != EstadoFisico.DETERIORADO;
    }

    // equals y hashCode basados en código de barras
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ejemplar ejemplar = (Ejemplar) o;
        return Objects.equals(codigoBarras, ejemplar.codigoBarras);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoBarras);
    }

    @Override
    public String toString() {
        return "Ejemplar{" +
                "idEjemplar=" + idEjemplar +
                ", codigoBarras='" + codigoBarras + '\'' +
                ", estadoFisico=" + estadoFisico +
                ", disponible=" + disponible +
                ", libro=" + (libro != null ? libro.getTitulo() : "N/A") +
                '}';
    }
}