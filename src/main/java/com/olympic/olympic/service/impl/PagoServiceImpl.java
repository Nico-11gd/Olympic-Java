package com.olympic.olympic.service.impl;

import com.olympic.olympic.dto.PagoRequest;
import com.olympic.olympic.dto.PagoResponse;
import com.olympic.olympic.entity.EstadoPago;
import com.olympic.olympic.entity.Pago;
import com.olympic.olympic.entity.Pedido;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.repository.PagoRepository;
import com.olympic.olympic.repository.PedidoRepository;
import com.olympic.olympic.service.PagoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PagoServiceImpl implements PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    public PagoServiceImpl(PagoRepository pagoRepository, PedidoRepository pedidoRepository) {
        this.pagoRepository = pagoRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listar() {
        return pagoRepository.findAllByOrderByIdDesc().stream()
                .map(PagoResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public PagoResponse crear(PagoRequest request) {
        Pago pago = new Pago();
        aplicarCambios(pago, request);
        pago.setCreatedAt(LocalDateTime.now());
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    @Transactional
    public PagoResponse actualizar(Integer id, PagoRequest request) {
        Pago pago = buscarPorId(id);
        aplicarCambios(pago, request);
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    @Transactional
    public PagoResponse cambiarEstado(Integer id, EstadoPago estado) {
        Pago pago = buscarPorId(id);
        pago.setEstado(estado);
        return PagoResponse.fromEntity(pagoRepository.save(pago));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Pago pago = buscarPorId(id);
        pagoRepository.delete(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Pedido> pedidosDisponibles() {
        return pedidoRepository.findAll();
    }

    private void aplicarCambios(Pago pago, PagoRequest request) {
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado"));
        pago.setPedido(pedido);
        pago.setMetodo(request.getMetodo());
        pago.setEstado(request.getEstado());
        pago.setTotal(request.getTotal());
    }

    private Pago buscarPorId(Integer id) {
        return pagoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado"));
    }
}
