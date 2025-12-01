package com.biblioteca.model.enums;

/**
 * Enumeración para el estado físico de los ejemplares
 */
public enum EstadoFisico {
    EXCELENTE("Excelente"),
    BUENO("Bueno"),
    DETERIORADO("Deteriorado"),
    PERDIDO("Perdido");

    private final String descripcion;

    EstadoFisico(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Convierte un String a EstadoFisico
     */
    public static EstadoFisico fromString(String texto) {
        for (EstadoFisico estado : EstadoFisico.values()) {
            if (estado.descripcion.equalsIgnoreCase(texto)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado físico no válido: " + texto);
    }
}