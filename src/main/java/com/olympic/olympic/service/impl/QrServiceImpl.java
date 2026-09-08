package com.olympic.olympic.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.olympic.olympic.entity.MetodoPago;
import com.olympic.olympic.service.QrService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

@Service
public class QrServiceImpl implements QrService {

    private static final String TELEFONO_NEQUI = "3001234567";
    private static final String TELEFONO_DAVIPLATA = "3001234567";
    private static final String CUENTA_TRANSFERENCIA = "456789123";
    private static final String TITULAR = "OLYMPIC STORE (C.I.)";

    @Override
    public byte[] generarPng(String contenido, int ancho, int alto) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(
                    contenido,
                    BarcodeFormat.QR_CODE,
                    ancho,
                    alto,
                    Map.of(EncodeHintType.MARGIN, 2));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException e) {
            throw new IllegalStateException("No se pudo generar el código QR.", e);
        }
    }

    @Override
    public String contenidoPago(MetodoPago metodo, Integer pedidoId, BigDecimal total) {
        String monto = "$" + total + " COP";
        String referencia = "Pedido #" + pedidoId;
        return switch (metodo) {
            case NEQUI -> "NEQUI\nCuenta: " + TELEFONO_NEQUI
                    + "\nValor: " + monto
                    + "\nReferencia: " + referencia
                    + "\nTitular: " + TITULAR;
            case DAVIPLATA -> "DAVIPLATA\nCelular: " + TELEFONO_DAVIPLATA
                    + "\nValor: " + monto
                    + "\nReferencia: " + referencia
                    + "\nTitular: " + TITULAR;
            case TRANSFERENCIA -> "TRANSFERENCIA\nBanco: Bancolombia\nCuenta ahorros: " + CUENTA_TRANSFERENCIA
                    + "\nTitular: " + TITULAR
                    + "\nValor: " + monto
                    + "\nReferencia: " + referencia;
            case TARJETA -> "PAGO TARJETA\n" + monto + "\nReferencia: " + referencia + " (PSE / Datafono)";
            case EFECTIVO -> "PAGO EN EFECTIVO al recibir\n" + referencia;
        };
    }
}