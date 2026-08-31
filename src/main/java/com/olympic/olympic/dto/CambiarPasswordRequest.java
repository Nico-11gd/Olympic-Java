package com.olympic.olympic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Paso 3: el usuario define su nueva contraseña usando el código ya validado.
 */
public class CambiarPasswordRequest {

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido (ej: usuario@dominio.com).")
    private String correo;

    @NotBlank(message = "El código es obligatorio.")
    @Size(min = 6, max = 6, message = "El código debe tener 6 dígitos.")
    private String codigo;

    @NotBlank(message = "La nueva contraseña es obligatoria.")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres.")
    private String nuevaPassword;

    public CambiarPasswordRequest() {
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

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }
}