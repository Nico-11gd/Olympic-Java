package com.olympic.olympic.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoMovimientoConverter implements AttributeConverter<TipoMovimiento, String> {

    @Override
    public String convertToDatabaseColumn(TipoMovimiento tipo) {
        if (tipo == null) {
            return null;
        }
        return tipo.getValor();
    }

    @Override
    public TipoMovimiento convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        for (TipoMovimiento tipo : TipoMovimiento.values()) {
            if (tipo.getValor().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de movimiento desconocido: " + valor);
    }
}