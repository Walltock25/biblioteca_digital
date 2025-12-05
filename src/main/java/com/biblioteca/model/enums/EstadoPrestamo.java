package com.biblioteca.model.enums;

public enum EstadoPrestamo {
    ACTIVO("Activo"),
    FINALIZADO("Finalizado"),
    ATRASADO("Atrasado");

    private final String descripcion;

    EstadoPrestamo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public static EstadoPrestamo fromString(String texto) {
        for (EstadoPrestamo estado : EstadoPrestamo.values()) {
            if (estado.descripcion.equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de préstamo no válido: " + texto);
    }
}