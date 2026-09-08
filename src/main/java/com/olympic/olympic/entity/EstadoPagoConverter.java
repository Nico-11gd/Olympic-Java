package com.olympic.olympic.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EstadoPagoConverter implements AttributeConverter<EstadoPago, String> {

    @Override
    public String convertToDatabaseColumn(EstadoPago estado) {
        if (estado == null) {
            return null;
        }
        return estado.getValor();
    }

    @Override
    public EstadoPago convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        for (EstadoPago estado : EstadoPago.values()) {
            if (estado.getValor().equalsIgnoreCase(valor)) {
                return estado;
            }
        }
        throw new IllegalArgumentException("Estado de pago desconocido: " + valor);
    }
}
