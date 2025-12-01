package com.biblioteca.model;

import com.biblioteca.model.enums.EstadoReserva;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa una reserva de libro.
 * Mapea la tabla 'Reservas' en FNBC.
 */
public class Reserva {

    private Integer idReserva;
    private LocalDateTime fechaReserva;
    private EstadoReserva estado;
    private Usuario usuario;
    private Libro libro;

    // Constructores
    public Reserva() {
        this.fechaReserva = LocalDateTime.now();
        this.estado = EstadoReserva.PENDIENTE;
    }

    public Reserva(Usuario usuario, Libro libro) {
        this();
        this.usuario = usuario;
        this.libro = libro;
    }

    // Getters y Setters
    public Integer getIdReserva() {
        return idReserva;
    }

    public void setIdReserva(Integer idReserva) {
        this.idReserva = idReserva;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return Objects.equals(idReserva, reserva.idReserva);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idReserva);
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "idReserva=" + idReserva +
                ", usuario=" + (usuario != null ? usuario.getNombreCompleto() : "N/A") +
                ", libro=" + (libro != null ? libro.getTitulo() : "N/A") +
                ", estado=" + estado +
                '}';
    }
}