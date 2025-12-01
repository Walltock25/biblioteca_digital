package com.biblioteca.dao;

import com.biblioteca.model.Prestamo;
import com.biblioteca.model.enums.EstadoPrestamo;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz para operaciones con Préstamos
 */
public interface PrestamoDAO extends GenericDAO<Prestamo, Integer> {

    /**
     * Busca préstamos de un usuario por estado
     */
    List<Prestamo> findByUsuarioAndEstado(Integer idUsuario, EstadoPrestamo estado) throws SQLException;

    /**
     * Busca todos los préstamos de un usuario
     */
    List<Prestamo> findByUsuario(Integer idUsuario) throws SQLException;

    /**
     * Busca préstamos por estado
     */
    List<Prestamo> findByEstado(EstadoPrestamo estado) throws SQLException;

    /**
     * Cuenta préstamos activos de un usuario
     */
    int countPrestamosByUsuarioAndEstado(Integer idUsuario, EstadoPrestamo estado) throws SQLException;
}