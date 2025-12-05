package com.biblioteca.controller;

import com.biblioteca.dao.LibroDAO;
import com.biblioteca.dao.MultaDAO;
import com.biblioteca.dao.PrestamoDAO;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.dao.impl.LibroDAOImpl;
import com.biblioteca.dao.impl.MultaDAOImpl;
import com.biblioteca.dao.impl.PrestamoDAOImpl;
import com.biblioteca.dao.impl.UsuarioDAOImpl;
import com.biblioteca.model.enums.EstadoPrestamo;
import com.biblioteca.util.AlertUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class DashboardController {

    @FXML private Label lblTotalLibros;
    @FXML private Label lblPrestamosActivos;
    @FXML private Label lblTotalUsuarios;
    @FXML private Label lblMultasPendientes;

    private final LibroDAO libroDAO = new LibroDAOImpl();
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
    private final PrestamoDAO prestamoDAO = new PrestamoDAOImpl();
    private final MultaDAO multaDAO = new MultaDAOImpl();

    @FXML
    public void initialize() {
        cargarEstadisticas();
    }

    private void cargarEstadisticas() {
        try {
            // Total de libros
            long totalLibros = libroDAO.count();
            lblTotalLibros.setText(String.valueOf(totalLibros));

            // Préstamos activos
            int prestamosActivos = prestamoDAO.findByEstado(EstadoPrestamo.ACTIVO).size();
            lblPrestamosActivos.setText(String.valueOf(prestamosActivos));

            // Total usuarios
            long totalUsuarios = usuarioDAO.count();
            lblTotalUsuarios.setText(String.valueOf(totalUsuarios));

            // Multas pendientes
            long multasPendientes = multaDAO.findAll().stream()
                    .filter(m -> m.getEstadoPago().getDescripcion().equals("Pendiente"))
                    .count();
            lblMultasPendientes.setText(String.valueOf(multasPendientes));

        } catch (SQLException e) {
            AlertUtils.mostrarErrorBD(e);
        }
    }
}