package com.biblioteca.model;

import com.biblioteca.model.enums.EstadoPago;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public class Multa {

    private Integer idMulta;
    private BigDecimal monto;
    private String motivo;
    private LocalDateTime fechaGeneracion;
    private EstadoPago estadoPago;
    private Prestamo prestamo;

    // Constructores
    public Multa() {
        this.fechaGeneracion = LocalDateTime.now();
        this.estadoPago = EstadoPago.PENDIENTE;
        this.motivo = "Retraso en devolución";
    }

    public Multa(Prestamo prestamo, double monto) {
        this();
        this.prestamo = prestamo;
        this.monto = BigDecimal.valueOf(monto);
    }

    // Getters y Setters
    public Integer getIdMulta() {
        return idMulta;
    }

    public void setIdMulta(Integer idMulta) {
        this.idMulta = idMulta;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public void setMonto(double monto) {
        this.monto = BigDecimal.valueOf(monto);
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }

    public EstadoPago getEstadoPago() {
        return estadoPago;
    }

    public void setEstadoPago(EstadoPago estadoPago) {
        this.estadoPago = estadoPago;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    /**
     * Marca la multa como pagada
     */
    public void marcarComoPagada() {
        this.estadoPago = EstadoPago.PAGADO;
    }

    /**
     * Verifica si la multa está pendiente de pago
     */
    public boolean estaPendiente() {
        return estadoPago == EstadoPago.PENDIENTE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Multa multa = (Multa) o;
        return Objects.equals(idMulta, multa.idMulta);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idMulta);
    }

    @Override
    public String toString() {
        return "Multa{" +
                "idMulta=" + idMulta +
                ", monto=" + monto +
                ", motivo='" + motivo + '\'' +
                ", estadoPago=" + estadoPago +
                ", fechaGeneracion=" + fechaGeneracion +
                '}';
    }
}