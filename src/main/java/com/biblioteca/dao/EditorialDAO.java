package com.biblioteca.dao;

import com.biblioteca.model.Editorial;
// Heredamos de GenericDAO para tener save(), update(), delete()
public interface EditorialDAO extends GenericDAO<Editorial, Integer> {
    // Aquí podrías agregar métodos extra si los necesitas
}