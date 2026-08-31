package com.olympic.olympic.entity;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoPromocion {
    PORCENTAJE("porcentaje"),
    VALOR_FIJO("valor_fijo");

    private final String valor;

    TipoPromocion(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
