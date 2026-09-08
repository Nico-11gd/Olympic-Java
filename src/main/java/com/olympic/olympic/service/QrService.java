package com.olympic.olympic.service;

import com.olympic.olympic.entity.MetodoPago;

import java.math.BigDecimal;

/**
 * Genera códigos QR reales (ZXing) con los datos de pago del pedido, para
 * que el cliente pueda escanearlos con Nequi, Daviplata, su banco o
 * cualquier lector de QR.
 */
public interface QrService {

    byte[] generarPng(String contenido, int ancho, int alto);

    String contenidoPago(MetodoPago metodo, Integer pedidoId, BigDecimal total);
}