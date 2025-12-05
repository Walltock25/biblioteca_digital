package com.biblioteca.dao.impl;

import com.biblioteca.dao.AutorDAO;
import com.biblioteca.dao.LibroDAO;
import com.biblioteca.model.*;
import com.biblioteca.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO para la entidad Libro.
 * Usa PreparedStatement para prevenir inyección SQL.
 */
public class LibroDAOImpl implements LibroDAO {

    // Instanciamos el DAO de autores para poder usarlo en la lectura
    private final AutorDAO autorDAO = new AutorDAOImpl();

    private static final String INSERT_LIBRO =
            "INSERT INTO Libros (isbn, titulo, anio_publicacion, id_editorial, id_categoria) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_LIBRO_AUTOR =
            "INSERT INTO Libro_Autor (id_libro, id_autor) VALUES (?, ?)";

    private static final String UPDATE_LIBRO =
            "UPDATE Libros SET isbn = ?, titulo = ?, anio_publicacion = ?, " +
                    "id_editorial = ?, id_categoria = ? WHERE id_libro = ?";

    private static final String DELETE_LIBRO =
            "DELETE FROM Libros WHERE id_libro = ?";

    private static final String SELECT_BY_ID =
            "SELECT l.*, e.nombre AS editorial_nombre, c.nombre AS categoria_nombre " +
                    "FROM Libros l " +
                    "INNER JOIN Editoriales e ON l.id_editorial = e.id_editorial " +
                    "INNER JOIN Categorias c ON l.id_categoria = c.id_categoria " +
                    "WHERE l.id_libro = ?";

    private static final String SELECT_ALL =
            "SELECT l.*, e.nombre AS editorial_nombre, c.nombre AS categoria_nombre " +
                    "FROM Libros l " +
                    "INNER JOIN Editoriales e ON l.id_editorial = e.id_editorial " +
                    "INNER JOIN Categorias c ON l.id_categoria = c.id_categoria " +
                    "ORDER BY l.titulo";

    private static final String SELECT_BY_ISBN =
            "SELECT l.*, e.nombre AS editorial_nombre, c.nombre AS categoria_nombre " +
                    "FROM Libros l " +
                    "INNER JOIN Editoriales e ON l.id_editorial = e.id_editorial " +
                    "INNER JOIN Categorias c ON l.id_categoria = c.id_categoria " +
                    "WHERE l.isbn = ?";

    private static final String SELECT_BY_TITULO =
            "SELECT l.*, e.nombre AS editorial_nombre, c.nombre AS categoria_nombre " +
                    "FROM Libros l " +
                    "INNER JOIN Editoriales e ON l.id_editorial = e.id_editorial " +
                    "INNER JOIN Categorias c ON l.id_categoria = c.id_categoria " +
                    "WHERE l.titulo LIKE ? " +
                    "ORDER BY l.titulo";

    private static final String COUNT_EJEMPLARES_DISPONIBLES =
            "SELECT COUNT(*) FROM Ejemplares " +
                    "WHERE id_libro = ? AND disponible = TRUE";

    @Override
    public Integer save(Libro libro) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_LIBRO,
                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, libro.getIsbn());
            stmt.setString(2, libro.getTitulo());
            stmt.setInt(3, libro.getAnioPublicacion());
            stmt.setInt(4, libro.getEditorial().getIdEditorial());
            stmt.setInt(5, libro.getCategoria().getIdCategoria());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("No se pudo insertar el libro");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("No se pudo obtener el ID generado");
                }
            }
        }
    }

    @Override
    public Integer saveWithAutores(Libro libro) throws SQLException {
        Connection conn = null;

        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Inicio de transacción

            // 1. Guardar el libro
            Integer idLibro;
            try (PreparedStatement stmtLibro = conn.prepareStatement(INSERT_LIBRO,
                    Statement.RETURN_GENERATED_KEYS)) {

                stmtLibro.setString(1, libro.getIsbn());
                stmtLibro.setString(2, libro.getTitulo());
                stmtLibro.setInt(3, libro.getAnioPublicacion());
                stmtLibro.setInt(4, libro.getEditorial().getIdEditorial());
                stmtLibro.setInt(5, libro.getCategoria().getIdCategoria());

                stmtLibro.executeUpdate();

                try (ResultSet generatedKeys = stmtLibro.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        idLibro = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("No se pudo obtener el ID del libro");
                    }
                }
            }

            // 2. Guardar las relaciones Libro-Autor
            if (libro.getAutores() != null && !libro.getAutores().isEmpty()) {
                try (PreparedStatement stmtAutor = conn.prepareStatement(INSERT_LIBRO_AUTOR)) {
                    for (Autor autor : libro.getAutores()) {
                        stmtAutor.setInt(1, idLibro);
                        stmtAutor.setInt(2, autor.getIdAutor());
                        stmtAutor.addBatch();
                    }
                    stmtAutor.executeBatch();
                }
            }

            conn.commit(); // Confirmar transacción
            return idLibro;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Revertir cambios en caso de error
                } catch (SQLException ex) {
                    throw new SQLException("Error al hacer rollback: " + ex.getMessage(), ex);
                }
            }
            throw new SQLException("Error al guardar libro con autores: " + e.getMessage(), e);

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Restaurar modo por defecto
                    conn.close();
                } catch (SQLException e) {
                    // Log del error
                }
            }
        }
    }

    @Override
    public boolean update(Libro libro) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_LIBRO)) {

            stmt.setString(1, libro.getIsbn());
            stmt.setString(2, libro.getTitulo());
            stmt.setInt(3, libro.getAnioPublicacion());
            stmt.setInt(4, libro.getEditorial().getIdEditorial());
            stmt.setInt(5, libro.getCategoria().getIdCategoria());
            stmt.setInt(6, libro.getIdLibro());

            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_LIBRO)) {

            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        }
    }

    @Override
    public Optional<Libro> findById(Integer id) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLibro(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Libro> findByIsbn(String isbn) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ISBN)) {

            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToLibro(rs));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public List<Libro> findByTitulo(String titulo) throws SQLException {
        List<Libro> libros = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_TITULO)) {

            stmt.setString(1, "%" + titulo + "%");

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    libros.add(mapResultSetToLibro(rs));
                }
            }
        }
        return libros;
    }

    @Override
    public List<Libro> findByCategoria(Integer idCategoria) throws SQLException {
        // Implementación futura si es necesaria
        return new ArrayList<>();
    }

    @Override
    public List<Libro> findByAutor(Integer idAutor) throws SQLException {
        // Implementación futura si es necesaria
        return new ArrayList<>();
    }

    @Override
    public List<Libro> findAll() throws SQLException {
        List<Libro> libros = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                libros.add(mapResultSetToLibro(rs));
            }
        }
        return libros;
    }

    @Override
    public long count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM Libros";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0;
    }

    @Override
    public boolean existsById(Integer id) throws SQLException {
        return findById(id).isPresent();
    }

    @Override
    public int countEjemplaresDisponibles(Integer idLibro) throws SQLException {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(COUNT_EJEMPLARES_DISPONIBLES)) {

            stmt.setInt(1, idLibro);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // Método adicional para actualizar autores al editar un libro
    // NOTA: Recuerda agregar este método a tu interfaz LibroDAO para evitar tener que hacer casting en el controlador.
    public void updateAutores(Libro libro) throws SQLException {
        String deleteSql = "DELETE FROM Libro_Autor WHERE id_libro = ?";
        String insertSql = "INSERT INTO Libro_Autor (id_libro, id_autor) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false); // Transacción

            try {
                // 1. Borrar relaciones existentes
                try (PreparedStatement stmtDel = conn.prepareStatement(deleteSql)) {
                    stmtDel.setInt(1, libro.getIdLibro());
                    stmtDel.executeUpdate();
                }

                // 2. Insertar las nuevas
                if (libro.getAutores() != null && !libro.getAutores().isEmpty()) {
                    try (PreparedStatement stmtIns = conn.prepareStatement(insertSql)) {
                        for (Autor autor : libro.getAutores()) {
                            stmtIns.setInt(1, libro.getIdLibro());
                            stmtIns.setInt(2, autor.getIdAutor());
                            stmtIns.addBatch();
                        }
                        stmtIns.executeBatch();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Mapea un ResultSet a un objeto Libro
     */
    private Libro mapResultSetToLibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        libro.setIdLibro(rs.getInt("id_libro"));
        libro.setIsbn(rs.getString("isbn"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setAnioPublicacion(rs.getInt("anio_publicacion"));

        // Mapear Editorial
        Editorial editorial = new Editorial();
        editorial.setIdEditorial(rs.getInt("id_editorial"));
        editorial.setNombre(rs.getString("editorial_nombre"));
        libro.setEditorial(editorial);

        // Mapear Categoría
        Categoria categoria = new Categoria();
        categoria.setIdCategoria(rs.getInt("id_categoria"));
        categoria.setNombre(rs.getString("categoria_nombre"));
        libro.setCategoria(categoria);

        // CORRECCIÓN: Usamos el DAO de autores para llenar la lista correctamente
        List<Autor> autores = autorDAO.findByLibro(libro.getIdLibro());
        libro.setAutores(autores);

        return libro;
    }
}