package com.olympic.olympic.repository;

import com.olympic.olympic.entity.InventarioMovimiento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventarioMovimientoRepository extends JpaRepository<InventarioMovimiento, Integer> {

    List<InventarioMovimiento> findAllByOrderByIdDesc();
}