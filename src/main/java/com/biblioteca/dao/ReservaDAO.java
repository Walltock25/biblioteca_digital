package com.biblioteca.dao;

import com.biblioteca.model.Reserva;
import com.biblioteca.model.enums.EstadoReserva;

import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz para operaciones con Reservas
 */
public interface ReservaDAO extends GenericDAO<Reserva, Integer> {

    /**
     * Busca reservas de un usuario por estado
     */
    List<Reserva> findByUsuarioAndEstado(Integer idUsuario, EstadoReserva estado) throws SQLException;

    /**
     * Busca todas las reservas de un usuario
     */
    List<Reserva> findByUsuario(Integer idUsuario) throws SQLException;

    /**
     * Busca reservas de un libro específico
     */
    List<Reserva> findByLibro(Integer idLibro) throws SQLException;

    /**
     * Busca reservas pendientes de un libro
     */
    List<Reserva> findReservasPendientesByLibro(Integer idLibro) throws SQLException;

    /**
     * Verifica si un usuario ya tiene una reserva activa para un libro
     */
    boolean usuarioTieneReservaActiva(Integer idUsuario, Integer idLibro) throws SQLException;

    /**
     * Cuenta reservas activas de un usuario
     */
    int countReservasActivasByUsuario(Integer idUsuario) throws SQLException;
}