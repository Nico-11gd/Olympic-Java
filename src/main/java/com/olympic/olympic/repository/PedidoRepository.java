package com.olympic.olympic.repository;

import com.olympic.olympic.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    List<Pedido> findByClienteIdOrderByCreatedAtDesc(Integer clienteId);

    List<Pedido> findByClienteIdOrderByIdDesc(Integer clienteId);
}
