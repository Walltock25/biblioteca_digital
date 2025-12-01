package com.biblioteca.model;

import com.biblioteca.model.enums.EstadoPrestamo;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Entidad que representa la TRANSACCIÓN de préstamo de un ejemplar.
 * Mapea la tabla 'Prestamos' en FNBC.
 */
public class Prestamo {

    private Integer idPrestamo;
    private LocalDateTime fechaSalida;
    private LocalDateTime fechaDevolucionEsperada;
    private LocalDateTime fechaDevolucionReal;
    private EstadoPrestamo estado;

    // Relaciones
    private Usuario usuario;
    private Ejemplar ejemplar;

    // Constructores
    public Prestamo() {
        this.fechaSalida = LocalDateTime.now();
        this.estado = EstadoPrestamo.ACTIVO;
    }

    public Prestamo(Usuario usuario, Ejemplar ejemplar, LocalDateTime fechaDevolucionEsperada) {
        this();
        this.usuario = usuario;
        this.ejemplar = ejemplar;
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
    }

    // Getters y Setters
    public Integer getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Integer idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public LocalDateTime getFechaSalida() {
        return fechaSalida;
    }

    public void setFechaSalida(LocalDateTime fechaSalida) {
        this.fechaSalida = fechaSalida;
    }

    public LocalDateTime getFechaDevolucionEsperada() {
        return fechaDevolucionEsperada;
    }

    public void setFechaDevolucionEsperada(LocalDateTime fechaDevolucionEsperada) {
        this.fechaDevolucionEsperada = fechaDevolucionEsperada;
    }

    public LocalDateTime getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(LocalDateTime fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public EstadoPrestamo getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Ejemplar getEjemplar() {
        return ejemplar;
    }

    public void setEjemplar(Ejemplar ejemplar) {
        this.ejemplar = ejemplar;
    }

    // Métodos de negocio

    /**
     * Verifica si el préstamo está atrasado
     */
    public boolean estaAtrasado() {
        if (estado == EstadoPrestamo.FINALIZADO) {
            return false;
        }
        return LocalDateTime.now().isAfter(fechaDevolucionEsperada);
    }

    /**
     * Calcula los días de retraso
     */
    public long calcularDiasRetraso() {
        if (!estaAtrasado()) {
            return 0;
        }

        LocalDateTime fechaReferencia = fechaDevolucionReal != null
                ? fechaDevolucionReal
                : LocalDateTime.now();

        return ChronoUnit.DAYS.between(fechaDevolucionEsperada, fechaReferencia);
    }

    /**
     * Marca el préstamo como devuelto
     */
    public void marcarComoDevuelto() {
        this.fechaDevolucionReal = LocalDateTime.now();
        this.estado = EstadoPrestamo.FINALIZADO;
    }

    // equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prestamo prestamo = (Prestamo) o;
        return Objects.equals(idPrestamo, prestamo.idPrestamo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPrestamo);
    }

    @Override
    public String toString() {
        return "Prestamo{" +
                "idPrestamo=" + idPrestamo +
                ", usuario=" + (usuario != null ? usuario.getNombre() : "N/A") +
                ", ejemplar=" + (ejemplar != null ? ejemplar.getCodigoBarras() : "N/A") +
                ", fechaSalida=" + fechaSalida +
                ", fechaDevolucionEsperada=" + fechaDevolucionEsperada +
                ", estado=" + estado +
                '}';
    }
}