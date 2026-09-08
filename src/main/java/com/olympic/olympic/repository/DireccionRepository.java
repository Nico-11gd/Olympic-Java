package com.olympic.olympic.repository;

import com.olympic.olympic.entity.Direccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DireccionRepository extends JpaRepository<Direccion, Integer> {

    List<Direccion> findByUsuarioIdAndActivoTrueOrderByEsPrincipalDescIdDesc(Integer usuarioId);
}