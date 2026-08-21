package com.olympic.olympic.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoPromocionConverter implements AttributeConverter<TipoPromocion, String> {

    @Override
    public String convertToDatabaseColumn(TipoPromocion tipo) {
        return tipo == null ? null : tipo.getValor();
    }

    @Override
    public TipoPromocion convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        for (TipoPromocion tipo : TipoPromocion.values()) {
            if (tipo.getValor().equalsIgnoreCase(valor)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de promoción desconocido: " + valor);
    }
}
