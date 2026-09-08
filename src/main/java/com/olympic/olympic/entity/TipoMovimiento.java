package com.olympic.olympic.entity;

import java.util.Arrays;

public enum TipoMovimiento {

    ENTRADA("entrada"),
    VENTA("venta"),
    DANO("daño"),
    DEVOLUCION("devolucion"),
    PERDIDA("perdida"),
    AJUSTE("ajuste");

    private final String valor;

    TipoMovimiento(String valor) {
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    public static TipoMovimiento fromValor(String valor) {
        return Arrays.stream(values())
                .filter(t -> t.getValor().equalsIgnoreCase(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de movimiento desconocido: " + valor));
    }
}