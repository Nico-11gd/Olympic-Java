package com.olympic.olympic.entity;

import com.fasterxml.jackson.annotation.JsonValue;

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
}