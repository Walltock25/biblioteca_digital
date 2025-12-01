package com.biblioteca.dao;

import com.biblioteca.model.Multa;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz para operaciones con Multas
 */
public interface MultaDAO extends GenericDAO<Multa, Integer> {

    /**
     * Busca multas de un usuario por estado de pago
     */
    List<Multa> findByUsuarioAndEstado(Integer idUsuario, String estadoPago) throws SQLException;

    /**
     * Busca todas las multas de un usuario
     */
    List<Multa> findByUsuario(Integer idUsuario) throws SQLException;

    /**
     * Busca la multa asociada a un préstamo
     */
    Multa findByPrestamo(Integer idPrestamo) throws SQLException;
}