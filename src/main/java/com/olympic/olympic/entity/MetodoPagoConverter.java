package com.olympic.olympic.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class MetodoPagoConverter implements AttributeConverter<MetodoPago, String> {

    @Override
    public String convertToDatabaseColumn(MetodoPago metodo) {
        if (metodo == null) {
            return null;
        }
        return metodo.getValor();
    }

    @Override
    public MetodoPago convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        for (MetodoPago metodo : MetodoPago.values()) {
            if (metodo.getValor().equalsIgnoreCase(valor)) {
                return metodo;
            }
        }
        throw new IllegalArgumentException("Método de pago desconocido: " + valor);
    }
}
