package com.biblioteca.dao;

import com.biblioteca.model.Libro;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz específica para operaciones con Libros.
 * Extiende GenericDAO y agrega métodos especializados.
 */
public interface LibroDAO extends GenericDAO<Libro, Integer> {

    /**
     * Busca un libro por ISBN (clave natural única)
     * @param isbn Código ISBN del libro
     * @return Optional con el libro si existe
     * @throws SQLException si hay error en la consulta
     */
    Optional<Libro> findByIsbn(String isbn) throws SQLException;

    /**
     * Busca libros por título (búsqueda parcial)
     * @param titulo Título o parte del título
     * @return Lista de libros que coinciden
     * @throws SQLException si hay error en la consulta
     */
    List<Libro> findByTitulo(String titulo) throws SQLException;

    /**
     * Busca libros por categoría
     * @param idCategoria ID de la categoría
     * @return Lista de libros en esa categoría
     * @throws SQLException si hay error en la consulta
     */
    List<Libro> findByCategoria(Integer idCategoria) throws SQLException;

    /**
     * Busca libros por autor
     * @param idAutor ID del autor
     * @return Lista de libros de ese autor
     * @throws SQLException si hay error en la consulta
     */
    List<Libro> findByAutor(Integer idAutor) throws SQLException;

    /**
     * Guarda un libro junto con sus autores (transacción completa).
     * Esta operación debe ser ATÓMICA: o se guarda todo o nada.
     *
     * @param libro Libro con la lista de autores cargada
     * @return ID del libro guardado
     * @throws SQLException si hay error (hace rollback automático)
     */
    Integer saveWithAutores(Libro libro) throws SQLException;

    /**
     * Cuenta cuántos ejemplares disponibles tiene un libro
     * @param idLibro ID del libro
     * @return Número de ejemplares disponibles
     * @throws SQLException si hay error en la consulta
     */
    int countEjemplaresDisponibles(Integer idLibro) throws SQLException;
}