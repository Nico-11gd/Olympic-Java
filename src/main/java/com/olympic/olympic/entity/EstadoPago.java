package com.olympic.olympic.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum EstadoPago {

    PENDIENTE("pendiente"),
    APROBADO("aprobado"),
    RECHAZADO("rechazado");

    private final String valor;

    EstadoPago(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static EstadoPago fromValor(String valor) {
        return Arrays.stream(values())
                .filter(e -> e.getValor().equalsIgnoreCase(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Estado de pago desconocido: " + valor));
    }
}
