package com.biblioteca.dao.impl;

import com.biblioteca.dao.CategoriaDAO;
import com.biblioteca.model.Categoria;
import com.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoriaDAOImpl implements CategoriaDAO {

    @Override
    public Integer save(Categoria entity) throws SQLException {
        String sql = "INSERT INTO Categorias (nombre, descripcion) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getNombre());
            stmt.setString(2, entity.getDescripcion());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    @Override
    public List<Categoria> findAll() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        String sql = "SELECT * FROM Categorias ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombre(rs.getString("nombre"));
                c.setDescripcion(rs.getString("descripcion"));
                lista.add(c);
            }
        }
        return lista;
    }

    // Métodos update y delete básicos (puedes completarlos si necesitas editar categorías)
    @Override public boolean update(Categoria entity) throws SQLException { return false; }
    @Override public boolean delete(Integer id) throws SQLException { return false; }
    @Override public Optional<Categoria> findById(Integer id) throws SQLException { return Optional.empty(); }
    @Override public long count() throws SQLException { return 0; }
    @Override public boolean existsById(Integer id) throws SQLException { return false; }
}