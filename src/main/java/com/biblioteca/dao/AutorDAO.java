package com.biblioteca.dao;

import com.biblioteca.model.Autor;
import java.sql.SQLException;
import java.util.List;

/**
 * Interfaz para operaciones con Autores
 */
public interface AutorDAO extends GenericDAO<Autor, Integer> {

    /**
     * Busca autores por nombre (búsqueda parcial)
     */
    List<Autor> findByNombre(String nombre) throws SQLException;

    /**
     * Busca autores por nacionalidad
     */
    List<Autor> findByNacionalidad(String nacionalidad) throws SQLException;

    /**
     * Obtiene todos los autores de un libro específico
     */
    List<Autor> findByLibro(Integer idLibro) throws SQLException;

    /**
     * Verifica si un autor tiene libros asociados
     */
    boolean tieneLibrosAsociados(Integer idAutor) throws SQLException;
}