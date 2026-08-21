package com.olympic.olympic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Paso 1: el usuario pide un código a su correo. */
public class SolicitarCodigoRequest {

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido (ej: usuario@dominio.com).")
    private String correo;

    public SolicitarCodigoRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}