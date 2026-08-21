package com.olympic.olympic.repository;

import com.olympic.olympic.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    List<Producto> findAllByOrderByIdDesc();

    // activo = true AND stock > 0 → equivalente a Producto::obtenerActivos()
    List<Producto> findByActivoTrueAndStockGreaterThanOrderByIdDesc(Integer stock);

    Optional<Producto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);
}
