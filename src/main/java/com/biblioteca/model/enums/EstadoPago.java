package com.biblioteca.model.enums;

public enum EstadoPago {
    PENDIENTE("Pendiente"),
    PAGADO("Pagado");

    private final String descripcion;

    EstadoPago(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoPago fromString(String texto) {
        for (EstadoPago estado : EstadoPago.values()) {
            if (estado.descripcion.equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pago no válido: " + texto);
    }
}