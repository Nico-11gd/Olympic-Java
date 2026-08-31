package com.olympic.olympic.repository;

import com.olympic.olympic.entity.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PromocionRepository extends JpaRepository<Promocion, Integer> {

    List<Promocion> findByActivoTrueOrderByIdDesc();
}
