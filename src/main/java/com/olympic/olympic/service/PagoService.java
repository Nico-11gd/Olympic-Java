package com.olympic.olympic.service;

import com.olympic.olympic.dto.PagoRequest;
import com.olympic.olympic.dto.PagoResponse;
import com.olympic.olympic.entity.Pedido;

import java.util.List;

public interface PagoService {

    List<PagoResponse> listar();

    PagoResponse crear(PagoRequest request);

    PagoResponse actualizar(Integer id, PagoRequest request);

    PagoResponse cambiarEstado(Integer id, com.olympic.olympic.entity.EstadoPago estado);

    void eliminar(Integer id);

    List<Pedido> pedidosDisponibles();
}
