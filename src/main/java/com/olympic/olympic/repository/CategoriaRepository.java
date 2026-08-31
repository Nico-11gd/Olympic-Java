package com.olympic.olympic.repository;

import com.olympic.olympic.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    List<Categoria> findByActivoTrueOrderByNombreAsc();
}
