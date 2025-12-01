package com.biblioteca.dao;

import com.biblioteca.model.Ejemplar;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz para operaciones con Ejemplares (copias físicas)
 */
public interface EjemplarDAO extends GenericDAO<Ejemplar, Integer> {

    /**
     * Busca un ejemplar por código de barras
     */
    Optional<Ejemplar> findByCodigoBarras(String codigoBarras) throws SQLException;

    /**
     * Busca ejemplares de un libro específico
     */
    List<Ejemplar> findByLibro(Integer idLibro) throws SQLException;

    /**
     * Busca ejemplares disponibles de un libro
     */
    List<Ejemplar> findDisponiblesByLibro(Integer idLibro) throws SQLException;
}