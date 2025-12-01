package com.biblioteca.model.enums;

/**
 * Enumeración para el estado de las reservas
 */
public enum EstadoReserva {
    PENDIENTE("Pendiente"),
    NOTIFICADO("Notificado"),
    CANCELADO("Cancelado"),
    COMPLETADO("Completado");

    private final String descripcion;

    EstadoReserva(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoReserva fromString(String texto) {
        for (EstadoReserva estado : EstadoReserva.values()) {
            if (estado.descripcion.equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de reserva no válido: " + texto);
    }
}