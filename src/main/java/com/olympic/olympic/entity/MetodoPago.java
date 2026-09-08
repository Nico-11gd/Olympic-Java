package com.olympic.olympic.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum MetodoPago {

    EFECTIVO("efectivo"),
    NEQUI("nequi"),
    DAVIPLATA("daviplata"),
    TRANSFERENCIA("transferencia"),
    TARJETA("tarjeta");

    private final String valor;

    MetodoPago(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static MetodoPago fromValor(String valor) {
        return Arrays.stream(values())
                .filter(m -> m.getValor().equalsIgnoreCase(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Método de pago desconocido: " + valor));
    }
}
