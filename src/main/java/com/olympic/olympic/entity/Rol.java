package com.olympic.olympic.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum Rol {

    ADMIN("admin"),
    CLIENTE("cliente");

    private final String valor;

    Rol(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }

    @JsonCreator
    public static Rol fromValor(String valor) {
        return Arrays.stream(values())
                .filter(r -> r.getValor().equalsIgnoreCase(valor))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Rol desconocido: " + valor));
    }
}