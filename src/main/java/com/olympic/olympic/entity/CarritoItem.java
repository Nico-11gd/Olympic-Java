package com.olympic.olympic.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Elemento del carrito de compras. Se almacena en la HttpSession como una
 * lista de estos objetos. No es una entidad JPA — es un POJO serializable.
 */
public class CarritoItem implements Serializable {

    private Integer productoId;
    private String nombre;
    private String imagenUrl;
    private String talla;
    private String color;
    private BigDecimal precioUnitario;
    private int cantidad;
    private BigDecimal descuentoPorcentaje;

    public CarritoItem() {
    }

    public CarritoItem(Integer productoId, String nombre, String imagenUrl,
                       String talla, String color, BigDecimal precioUnitario,
                       int cantidad, BigDecimal descuentoPorcentaje) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.imagenUrl = imagenUrl;
        this.talla = talla;
        this.color = color;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
        this.descuentoPorcentaje = descuentoPorcentaje != null ? descuentoPorcentaje : BigDecimal.ZERO;
    }

    public BigDecimal getSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getValorDescuento() {
        if (descuentoPorcentaje == null || descuentoPorcentaje.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getSubtotal().multiply(descuentoPorcentaje)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotal() {
        return getSubtotal().subtract(getValorDescuento()).setScale(2, RoundingMode.HALF_UP);
    }

    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }
}
