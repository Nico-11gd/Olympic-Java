package com.olympic.olympic.repository;

import com.olympic.olympic.entity.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    List<DetallePedido> findByPedidoIdOrderByIdAsc(Integer pedidoId);
}