package com.olympic.olympic.dto;

import com.olympic.olympic.entity.EstadoPago;
import com.olympic.olympic.entity.MetodoPago;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Datos para crear/actualizar un pago. El pedido se elige por id y el total,
 * método y estado se registran manualmente.
 */
public class PagoRequest {

    @NotNull(message = "Selecciona un pedido.")
    private Integer pedidoId;

    @NotNull(message = "El método de pago es obligatorio.")
    private MetodoPago metodo;

    @NotNull(message = "El estado es obligatorio.")
    private EstadoPago estado;

    @NotNull(message = "El total es obligatorio.")
    @DecimalMin(value = "0.01", message = "El total debe ser mayor que 0.")
    private BigDecimal total;

    public PagoRequest() {
    }

    public Integer getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Integer pedidoId) {
        this.pedidoId = pedidoId;
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
}
