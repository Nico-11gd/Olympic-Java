package com.olympic.olympic.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/** Determina qué sugerencias de talla se muestran según la categoría (ver SelectorEtiquetas en el TSX original). */
public enum TipoTalla {
    NINGUNA("ninguna"),
    ROPA("ropa"),
    CALZADO("calzado");

    private final String valor;

    TipoTalla(String valor) {
        this.valor = valor;
    }

    @JsonValue
    public String getValor() {
        return valor;
    }
}
