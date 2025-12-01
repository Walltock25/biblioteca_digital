package com.biblioteca.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz genérica para operaciones CRUD estándar.
 * Sigue el patrón DAO (Data Access Object) para abstraer la persistencia.
 *
 * @param <T> Tipo de la entidad
 * @param <ID> Tipo de la clave primaria
 */
public interface GenericDAO<T, ID> {

    /**
     * Inserta una nueva entidad en la base de datos
     * @param entity Entidad a insertar
     * @return ID generado
     * @throws SQLException si hay error en la operación
     */
    ID save(T entity) throws SQLException;

    /**
     * Actualiza una entidad existente
     * @param entity Entidad con datos actualizados
     * @return true si se actualizó correctamente
     * @throws SQLException si hay error en la operación
     */
    boolean update(T entity) throws SQLException;

    /**
     * Elimina una entidad por su ID
     * @param id Identificador de la entidad
     * @return true si se eliminó correctamente
     * @throws SQLException si hay error en la operación
     */
    boolean delete(ID id) throws SQLException;

    /**
     * Busca una entidad por su ID
     * @param id Identificador de la entidad
     * @return Optional con la entidad si existe, vacío si no
     * @throws SQLException si hay error en la operación
     */
    Optional<T> findById(ID id) throws SQLException;

    /**
     * Obtiene todas las entidades
     * @return Lista de todas las entidades
     * @throws SQLException si hay error en la operación
     */
    List<T> findAll() throws SQLException;

    /**
     * Cuenta el total de registros
     * @return Número total de entidades
     * @throws SQLException si hay error en la operación
     */
    long count() throws SQLException;

    /**
     * Verifica si existe una entidad con el ID especificado
     * @param id Identificador a verificar
     * @return true si existe
     * @throws SQLException si hay error en la operación
     */
    boolean existsById(ID id) throws SQLException;
}