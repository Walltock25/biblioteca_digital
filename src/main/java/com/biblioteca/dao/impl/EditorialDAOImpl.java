package com.biblioteca.dao.impl;

import com.biblioteca.dao.EditorialDAO;
import com.biblioteca.model.Editorial;
import com.biblioteca.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EditorialDAOImpl implements EditorialDAO {

    @Override
    public Integer save(Editorial entity) throws SQLException {
        String sql = "INSERT INTO Editoriales (nombre, pais, website) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, entity.getNombre());
            stmt.setString(2, entity.getPais());
            stmt.setString(3, entity.getWebsite());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return null;
    }

    @Override
    public List<Editorial> findAll() throws SQLException {
        // ... (el código que ya tenías para listar) ...
        List<Editorial> lista = new ArrayList<>();
        String sql = "SELECT * FROM Editoriales ORDER BY nombre";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Editorial e = new Editorial();
                e.setIdEditorial(rs.getInt("id_editorial"));
                e.setNombre(rs.getString("nombre"));
                e.setPais(rs.getString("pais"));
                e.setWebsite(rs.getString("website"));
                lista.add(e);
            }
        }
        return lista;
    }

    // Implementaciones vacías o simples para cumplir con la interfaz por ahora
    @Override public boolean update(Editorial entity) throws SQLException { return false; }
    @Override public boolean delete(Integer id) throws SQLException { return false; }
    @Override public Optional<Editorial> findById(Integer id) throws SQLException { return Optional.empty(); }
    @Override public long count() throws SQLException { return 0; }
    @Override public boolean existsById(Integer id) throws SQLException { return false; }
}