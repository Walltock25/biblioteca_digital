package com.biblioteca.dao.impl;

import com.biblioteca.dao.AutorDAO;
import com.biblioteca.model.Autor;
import com.biblioteca.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de AutorDAO con mejores prácticas
 */
public class AutorDAOImpl implements AutorDAO {

    private static final Logger logger = LoggerFactory.getLogger(AutorDAOImpl.class);

    private static final String INSERT =
            "INSERT INTO Autores (nombre, nacionalidad) VALUES (?, ?)";

    private static final String UPDATE =
            "UPDATE Autores SET nombre = ?, nacionalidad = ? WHERE id_autor = ?";

    private static final String DELETE =
            "DELETE FROM Autores WHERE id_autor = ?";

    private static final String SELECT_BY_ID =
            "SELECT * FROM Autores WHERE id_autor = ?";

    private static final String SELECT_ALL =
            "SELECT * FROM Autores ORDER BY nombre";

    private static final String SELECT_BY_NOMBRE =
            "SELECT * FROM Autores WHERE nombre LIKE ? ORDER BY nombre";

    private static final String SELECT_BY_NACIONALIDAD =
            "SELECT * FROM Autores WHERE nacionalidad = ? ORDER BY nombre";

    private static final String SELECT_BY_LIBRO =
            "SELECT a.* FROM Autores a " +
                    "INNER JOIN Libro_Autor la ON a.id_autor = la.id_autor " +
                    "WHERE la.id_libro = ? ORDER BY a.nombre";

    private static final String COUNT_LIBROS =
            "SELECT COUNT(*) FROM Libro_Autor WHERE id_autor = ?";

    @Override
    public Integer save(Autor autor) throws SQLException {
        logger.debug("Guardando autor: {}", autor.getNombre());

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, autor.getNombre());
            stmt.setString(2, autor.getNacionalidad());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo guardar el autor");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Integer id = generatedKeys.getInt(1);
                    logger.info("Autor guardado exitosamente con ID: {}", id);
                    return id;
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        } catch (SQLException e) {
            logger.error("Error al guardar autor: {}", autor, e);
            throw e;
        }
    }

    @Override
    public boolean update(Autor autor) throws SQLException {
        logger.debug("Actualizando autor ID: {}", autor.getIdAutor());

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE)) {

            stmt.setString(1, autor.getNombre());
            stmt.setString(2, autor.getNacionalidad());
            stmt.setInt(3, autor.getIdAutor());

            boolean updated = stmt.executeUpdate() > 0;

            if (updated) {
                logger.info("Autor actualizado: {}", autor.getIdAutor());
            } else {
                logger.warn("No se encontró autor con ID: {}", autor.getIdAutor());
            }

            return updated;

        } catch (SQLException e) {
            logger.error("Error al actualizar autor: {}", autor, e);
            throw e;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        logger.debug("Eliminando autor ID: {}", id);

        // Verificar si tiene libros asociados
        if (tieneLibrosAsociados(id)) {
            logger.warn("No se puede eliminar autor {} - tiene libros asociados", id);
            throw new SQLException("No se puede eliminar el autor porque tiene libros asociados");
        }

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE)) {

            stmt.setInt(1, id);
            boolean deleted = stmt.executeUpdate() > 0;

            if (deleted) {
                logger.info("Autor eliminado: {}", id);
            }

            return deleted;

        } catch (SQLException e) {
            logger.error("Error al eliminar autor ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public Optional<Autor> findById(Integer id) throws SQLException {
        logger.debug("Buscando autor por ID: {}", id);

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAutor(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Error al buscar autor por ID: {}", id, e);
            throw e;
        }

        return Optional.empty();
    }

    @Override
    public List<Autor> findAll() throws SQLException {
        logger.debug("Obteniendo todos los autores");

        List<Autor> autores = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                autores.add(mapResultSetToAutor(rs));
            }

            logger.debug("Se encontraron {} autores", autores.size());

        } catch (SQLException e) {
            logger.error("Error al obtener todos los autores", e);
            throw e;
        }

        return autores;
    }

    @Override
    public List<Autor> findByNombre(String nombre) throws SQLException {
        logger.debug("Buscando autores por nombre: {}", nombre);

        List<Autor> autores = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NOMBRE)) {

            stmt.setString(1, "%" + nombre + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autores.add(mapResultSetToAutor(rs));
                }
            }

            logger.debug("Se encontraron {} autores con nombre similar a '{}'", autores.size(), nombre);

        } catch (SQLException e) {
            logger.error("Error al buscar autores por nombre: {}", nombre, e);
            throw e;
        }

        return autores;
    }

    @Override
    public List<Autor> findByNacionalidad(String nacionalidad) throws SQLException {
        logger.debug("Buscando autores por nacionalidad: {}", nacionalidad);

        List<Autor> autores = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NACIONALIDAD)) {

            stmt.setString(1, nacionalidad);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autores.add(mapResultSetToAutor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar autores por nacionalidad: {}", nacionalidad, e);
            throw e;
        }

        return autores;
    }

    @Override
    public List<Autor> findByLibro(Integer idLibro) throws SQLException {
        logger.debug("Buscando autores del libro ID: {}", idLibro);

        List<Autor> autores = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_LIBRO)) {

            stmt.setInt(1, idLibro);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    autores.add(mapResultSetToAutor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar autores del libro: {}", idLibro, e);
            throw e;
        }

        return autores;
    }

    @Override
    public boolean tieneLibrosAsociados(Integer idAutor) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_LIBROS)) {

            stmt.setInt(1, idAutor);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }

        return false;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Autores";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Error al contar autores", e);
            throw e;
        }

        return 0;
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        return findById(id).isPresent();
    }

    /**
     * Mapea un ResultSet a un objeto Autor
     */
    private Autor mapResultSetToAutor(ResultSet rs) throws SQLException {
        Autor autor = new Autor();
        autor.setIdAutor(rs.getInt("id_autor"));
        autor.setNombre(rs.getString("nombre"));
        autor.setNacionalidad(rs.getString("nacionalidad"));
        return autor;
    }
}