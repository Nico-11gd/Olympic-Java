package com.olympic.olympic.service;

import com.olympic.olympic.entity.CarritoItem;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de carrito de compras. Almacena los items en la HttpSession del
 * usuario. Cada operación modifica la sesión y redirige para recargar la página.
 */
public interface CarritoService {

    void agregar(CarritoItem item, List<CarritoItem> carrito);

    void actualizarCantidad(int index, int cantidad, List<CarritoItem> carrito);

    void eliminar(int index, List<CarritoItem> carrito);

    void limpiar(List<CarritoItem> carrito);

    int contarItems(List<CarritoItem> carrito);

    BigDecimal calcularSubtotal(List<CarritoItem> carrito);

    BigDecimal calcularDescuentoTotal(List<CarritoItem> carrito);

    BigDecimal calcularIVA(List<CarritoItem> carrito);

    BigDecimal calcularTotal(List<CarritoItem> carrito);
}
