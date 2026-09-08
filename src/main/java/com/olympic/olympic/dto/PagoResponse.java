package com.olympic.olympic.dto;

import com.olympic.olympic.entity.EstadoPago;
import com.olympic.olympic.entity.MetodoPago;
import com.olympic.olympic.entity.Pago;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Representación pública de un Pago, incluyendo la referencia al pedido
 * asociado (solo su id y total para mostrarlo en la vista).
 */
public class PagoResponse {

    private Integer id;
    private Integer pedidoId;
    private BigDecimal pedidoTotal;
    private MetodoPago metodo;
    private EstadoPago estado;
    private BigDecimal total;
    private LocalDateTime createdAt;
    private String nombre;
    private String clienteCorreo;
    private String numero;

    public PagoResponse() {
    }

    public static PagoResponse fromEntity(Pago pago) {
        PagoResponse dto = new PagoResponse();
        dto.setId(pago.getId());
        if (pago.getPedido() != null) {
            dto.setPedidoId(pago.getPedido().getId());
            dto.setPedidoTotal(pago.getPedido().getTotal());
        }
        dto.setMetodo(pago.getMetodo());
        dto.setEstado(pago.getEstado());
        dto.setTotal(pago.getTotal());
        dto.setCreatedAt(pago.getCreatedAt());
        dto.setNombre(pago.getNombre());
        dto.setClienteCorreo(pago.getClienteCorreo());
        dto.setNumero(pago.getNumero());
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
    }

    public BigDecimal getPedidoTotal() {
        return pedidoTotal;
    }

    public void setPedidoTotal(BigDecimal pedidoTotal) {
        this.pedidoTotal = pedidoTotal;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public void setEstado(EstadoPago estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getClienteCorreo() {
        return clienteCorreo;
    }

    public void setClienteCorreo(String clienteCorreo) {
        this.clienteCorreo = clienteCorreo;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
