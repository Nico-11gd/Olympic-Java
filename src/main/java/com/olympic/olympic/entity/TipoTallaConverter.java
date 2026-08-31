package com.olympic.olympic.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoTallaConverter implements AttributeConverter<TipoTalla, String> {

    @Override
    public String convertToDatabaseColumn(TipoTalla tipo) {
        return tipo == null ? null : tipo.getValor();
    }

    @Override
    public TipoTalla convertToEntityAttribute(String valor) {
        if (valor == null) {
            return TipoTalla.NINGUNA;
        }
        for (TipoTalla tipo : TipoTalla.values()) {
            if (tipo.getValor().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        return TipoTalla.NINGUNA;
    }
}
