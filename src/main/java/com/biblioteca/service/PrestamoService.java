package com.biblioteca.service;

import com.biblioteca.dao.*;
import com.biblioteca.dao.impl.*;
import com.biblioteca.model.*;
import com.biblioteca.model.enums.EstadoPrestamo;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para gestionar la lógica de negocio de préstamos.
 * Coordina múltiples DAOs y aplica reglas de negocio complejas.
 */
public class PrestamoService {

    private final PrestamoDAO prestamoDAO;
    private final EjemplarDAO ejemplarDAO;
    private final UsuarioDAO usuarioDAO;
    private final MultaDAO multaDAO;

    // Constantes de negocio
    private static final int DIAS_PRESTAMO_DEFAULT = 14;
    private static final int MAX_PRESTAMOS_SIMULTANEOS = 3;
    private static final double MULTA_POR_DIA = 5.0;

    public PrestamoService() {
        this.prestamoDAO = new PrestamoDAOImpl();
        this.ejemplarDAO = new EjemplarDAOImpl();
        this.usuarioDAO = new UsuarioDAOImpl();
        this.multaDAO = new MultaDAOImpl();
    }

    /**
     * MÉTODO PRINCIPAL: Realiza el préstamo de un ejemplar a un usuario.
     *
     * Validaciones aplicadas:
     * 1. El usuario no puede tener multas pendientes
     * 2. El usuario no puede exceder el límite de préstamos simultáneos
     * 3. El ejemplar debe estar disponible
     * 4. El ejemplar debe estar en condiciones de ser prestado
     *
     * @param idUsuario ID del usuario solicitante
     * @param idEjemplar ID del ejemplar a prestar
     * @return El préstamo creado
     * @throws IllegalStateException si no se cumplen las validaciones
     * @throws SQLException si hay error en la base de datos
     */
    public Prestamo prestarLibro(Integer idUsuario, Integer idEjemplar)
            throws SQLException, IllegalStateException {

        // 1. VALIDAR QUE EL USUARIO EXISTE
        Usuario usuario = usuarioDAO.findById(idUsuario)
                .orElseThrow(() -> new IllegalStateException(
                        "Usuario no encontrado con ID: " + idUsuario));

        // 2. VALIDAR QUE NO TENGA MULTAS PENDIENTES
        if (usuarioTieneMultasPendientes(idUsuario)) {
            throw new IllegalStateException(
                    "El usuario " + usuario.getNombre() + " " + usuario.getApellido() +
                            " tiene multas pendientes. Debe pagar antes de solicitar un nuevo préstamo.");
        }

        // 3. VALIDAR LÍMITE DE PRÉSTAMOS SIMULTÁNEOS
        int prestamosActivos = contarPrestamosActivos(idUsuario);
        if (prestamosActivos >= MAX_PRESTAMOS_SIMULTANEOS) {
            throw new IllegalStateException(
                    "El usuario ha alcanzado el límite de " + MAX_PRESTAMOS_SIMULTANEOS +
                            " préstamos simultáneos. Préstamos activos: " + prestamosActivos);
        }

        // 4. VALIDAR QUE EL EJEMPLAR EXISTE
        Ejemplar ejemplar = ejemplarDAO.findById(idEjemplar)
                .orElseThrow(() -> new IllegalStateException(
                        "Ejemplar no encontrado con ID: " + idEjemplar));

        // 5. VALIDAR QUE EL EJEMPLAR ESTÁ DISPONIBLE
        if (!ejemplar.getDisponible()) {
            throw new IllegalStateException(
                    "El ejemplar con código de barras " + ejemplar.getCodigoBarras() +
                            " no está disponible actualmente.");
        }

        // 6. VALIDAR CONDICIÓN FÍSICA DEL EJEMPLAR
        if (!ejemplar.puedeSerPrestado()) {
            throw new IllegalStateException(
                    "El ejemplar no puede ser prestado debido a su estado físico: " +
                            ejemplar.getEstadoFisico());
        }

        // 7. CREAR EL PRÉSTAMO
        LocalDateTime fechaDevolucion = LocalDateTime.now().plusDays(DIAS_PRESTAMO_DEFAULT);
        Prestamo prestamo = new Prestamo(usuario, ejemplar, fechaDevolucion);

        try {
            // 8. GUARDAR EL PRÉSTAMO (esto inicia una transacción implícita)
            Integer idPrestamo = prestamoDAO.save(prestamo);
            prestamo.setIdPrestamo(idPrestamo);

            // 9. MARCAR EL EJEMPLAR COMO NO DISPONIBLE
            ejemplar.setDisponible(false);
            boolean ejemplarActualizado = ejemplarDAO.update(ejemplar);

            if (!ejemplarActualizado) {
                throw new SQLException("No se pudo actualizar la disponibilidad del ejemplar");
            }

            return prestamo;

        } catch (SQLException e) {
            throw new SQLException(
                    "Error al realizar el préstamo: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa la devolución de un libro.
     * Si hay retraso, genera automáticamente una multa.
     *
     * @param idPrestamo ID del préstamo a finalizar
     * @return true si la devolución fue exitosa
     * @throws SQLException si hay error en la base de datos
     */
    public boolean devolverLibro(Integer idPrestamo) throws SQLException {

        // 1. OBTENER EL PRÉSTAMO
        Prestamo prestamo = prestamoDAO.findById(idPrestamo)
                .orElseThrow(() -> new IllegalStateException(
                        "Préstamo no encontrado con ID: " + idPrestamo));

        // 2. VALIDAR QUE EL PRÉSTAMO ESTÁ ACTIVO
        if (prestamo.getEstado() != EstadoPrestamo.ACTIVO &&
                prestamo.getEstado() != EstadoPrestamo.ATRASADO) {
            throw new IllegalStateException(
                    "El préstamo ya fue finalizado anteriormente");
        }

        // 3. MARCAR COMO DEVUELTO
        prestamo.marcarComoDevuelto();

        // 4. GENERAR MULTA SI HAY RETRASO
        if (prestamo.calcularDiasRetraso() > 0) {
            generarMultaPorRetraso(prestamo);
        }

        // 5. ACTUALIZAR EL PRÉSTAMO EN BD
        boolean prestamoActualizado = prestamoDAO.update(prestamo);

        // 6. LIBERAR EL EJEMPLAR
        Ejemplar ejemplar = prestamo.getEjemplar();
        ejemplar.setDisponible(true);
        boolean ejemplarActualizado = ejemplarDAO.update(ejemplar);

        return prestamoActualizado && ejemplarActualizado;
    }

    /**
     * Verifica si un usuario tiene multas pendientes de pago
     */
    private boolean usuarioTieneMultasPendientes(Integer idUsuario) throws SQLException {
        List<Multa> multasPendientes = multaDAO.findByUsuarioAndEstado(
                idUsuario, "Pendiente");
        return !multasPendientes.isEmpty();
    }

    /**
     * Cuenta cuántos préstamos activos tiene un usuario
     */
    private int contarPrestamosActivos(Integer idUsuario) throws SQLException {
        return prestamoDAO.countPrestamosByUsuarioAndEstado(
                idUsuario, EstadoPrestamo.ACTIVO);
    }

    /**
     * Genera una multa automática por retraso en la devolución
     */
    private void generarMultaPorRetraso(Prestamo prestamo) throws SQLException {
        long diasRetraso = prestamo.calcularDiasRetraso();
        double montoMulta = diasRetraso * MULTA_POR_DIA;

        Multa multa = new Multa();
        multa.setPrestamo(prestamo);
        multa.setMonto(montoMulta);
        multa.setMotivo("Retraso de " + diasRetraso + " días en la devolución");

        multaDAO.save(multa);
    }

    /**
     * Obtiene todos los préstamos activos de un usuario
     */
    public List<Prestamo> obtenerPrestamosActivosDeUsuario(Integer idUsuario)
            throws SQLException {
        return prestamoDAO.findByUsuarioAndEstado(idUsuario, EstadoPrestamo.ACTIVO);
    }

    /**
     * Obtiene todos los préstamos atrasados del sistema
     */
    public List<Prestamo> obtenerPrestamosAtrasados() throws SQLException {
        return prestamoDAO.findByEstado(EstadoPrestamo.ATRASADO);
    }

    /**
     * Actualiza el estado de préstamos a "ATRASADO" si superaron la fecha límite.
     * Este método debería ejecutarse periódicamente (por ejemplo, diariamente).
     */
    public void actualizarPrestamosAtrasados() throws SQLException {
        List<Prestamo> prestamosActivos = prestamoDAO.findByEstado(EstadoPrestamo.ACTIVO);

        for (Prestamo prestamo : prestamosActivos) {
            if (prestamo.estaAtrasado()) {
                prestamo.setEstado(EstadoPrestamo.ATRASADO);
                prestamoDAO.update(prestamo);
            }
        }
    }
}