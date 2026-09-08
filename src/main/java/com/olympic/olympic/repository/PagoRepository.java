package com.olympic.olympic.repository;

import com.olympic.olympic.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    List<Pago> findAllByOrderByIdDesc();

    Optional<Pago> findTopByPedidoIdOrderByIdDesc(Integer pedidoId);

    boolean existsByPedidoId(Integer pedidoId);

    List<Pago> findByEstadoOrderByIdDesc(String estado);

    List<Pago> findByUsuarioIdOrderByIdDesc(Integer usuarioId);
}
