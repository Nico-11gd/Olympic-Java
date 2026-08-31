package com.olympic.olympic.repository;

import com.olympic.olympic.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findAllByOrderByIdDesc();

    List<Producto> findByActivoTrueOrderByNombreAsc();

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);

    List<Producto> findByCategoriaIdAndActivoTrueOrderByIdDesc(Integer categoriaId);

    List<Producto> findByCodigoStartingWith(String prefijo);
}
