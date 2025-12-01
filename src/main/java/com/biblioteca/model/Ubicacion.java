package com.biblioteca.model;

import java.util.Objects;

/**
 * Entidad que representa la ubicación física de un ejemplar.
 * Mapea la tabla 'Ubicaciones' en FNBC.
 */
public class Ubicacion {

    private Integer idUbicacion;
    private String pasillo;
    private String estante;
    private Integer piso;

    // Constructores
    public Ubicacion() {}

    public Ubicacion(String pasillo, String estante, Integer piso) {
        this.pasillo = pasillo;
        this.estante = estante;
        this.piso = piso;
    }

    // Getters y Setters
    public Integer getIdUbicacion() {
        return idUbicacion;
    }

    public void setIdUbicacion(Integer idUbicacion) {
        this.idUbicacion = idUbicacion;
    }

    public String getPasillo() {
        return pasillo;
    }

    public void setPasillo(String pasillo) {
        this.pasillo = pasillo;
    }

    public String getEstante() {
        return estante;
    }

    public void setEstante(String estante) {
        this.estante = estante;
    }

    public Integer getPiso() {
        return piso;
    }

    public void setPiso(Integer piso) {
        this.piso = piso;
    }

    /**
     * Retorna la ubicación en formato legible
     */
    public String getUbicacionCompleta() {
        return "Piso " + piso + " - Pasillo " + pasillo + " - Estante " + estante;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ubicacion ubicacion = (Ubicacion) o;
        return Objects.equals(pasillo, ubicacion.pasillo) &&
                Objects.equals(estante, ubicacion.estante) &&
                Objects.equals(piso, ubicacion.piso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pasillo, estante, piso);
    }

    @Override
    public String toString() {
        return getUbicacionCompleta();
    }
}