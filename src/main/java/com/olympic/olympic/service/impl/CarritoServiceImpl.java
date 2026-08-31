package com.olympic.olympic.service.impl;

import com.olympic.olympic.entity.CarritoItem;
import com.olympic.olympic.service.CarritoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CarritoServiceImpl implements CarritoService {

    private static final BigDecimal IVA_RATE = new BigDecimal("0.19");

    @Override
    public void agregar(CarritoItem item, List<CarritoItem> carrito) {
        for (CarritoItem existente : carrito) {
            if (existente.getProductoId().equals(item.getProductoId())
                    && stringIgual(existente.getTalla(), item.getTalla())
                    && stringIgual(existente.getColor(), item.getColor())) {
                existente.setCantidad(existente.getCantidad() + item.getCantidad());
                return;
            }
        }
        carrito.add(item);
    }

    @Override
    public void actualizarCantidad(int index, int cantidad, List<CarritoItem> carrito) {
        if (index >= 0 && index < carrito.size()) {
            if (cantidad <= 0) {
                carrito.remove(index);
            } else {
                carrito.get(index).setCantidad(cantidad);
            }
        }
    }

    @Override
    public void eliminar(int index, List<CarritoItem> carrito) {
        if (index >= 0 && index < carrito.size()) {
            carrito.remove(index);
        }
    }

    @Override
    public void limpiar(List<CarritoItem> carrito) {
        carrito.clear();
    }

    @Override
    public int contarItems(List<CarritoItem> carrito) {
        return carrito.stream().mapToInt(CarritoItem::getCantidad).sum();
    }

    @Override
    public BigDecimal calcularSubtotal(List<CarritoItem> carrito) {
        return carrito.stream()
                .map(CarritoItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularDescuentoTotal(List<CarritoItem> carrito) {
        return carrito.stream()
                .map(CarritoItem::getValorDescuento)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularIVA(List<CarritoItem> carrito) {
        BigDecimal base = calcularSubtotal(carrito).subtract(calcularDescuentoTotal(carrito));
        return base.multiply(IVA_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calcularTotal(List<CarritoItem> carrito) {
        return calcularSubtotal(carrito)
                .subtract(calcularDescuentoTotal(carrito))
                .add(calcularIVA(carrito))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private boolean stringIgual(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.trim().equalsIgnoreCase(b.trim());
    }
}
