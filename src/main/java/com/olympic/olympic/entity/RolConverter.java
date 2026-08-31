package com.olympic.olympic.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RolConverter implements AttributeConverter<Rol, String> {

    @Override
    public String convertToDatabaseColumn(Rol rol) {
        if (rol == null) {
            return null;
        }

        return rol.getValor();
    }

    @Override
    public Rol convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }

        for (Rol rol : Rol.values()) {
            if (rol.getValor().equalsIgnoreCase(valor)) {
                return rol;
            }
        }

        throw new IllegalArgumentException("Rol desconocido: " + valor);
    }
}