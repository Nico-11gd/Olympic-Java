package com.olympic.olympic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Paso 2: el usuario ingresa el código de 6 dígitos que le llegó al correo. */
public class ValidarCodigoRequest {

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido (ej: usuario@dominio.com).")
    private String correo;

    @NotBlank(message = "El código es obligatorio.")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos.")
    private String codigo;

    public ValidarCodigoRequest() {
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}