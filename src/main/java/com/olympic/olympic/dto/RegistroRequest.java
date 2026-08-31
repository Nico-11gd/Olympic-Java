package com.olympic.olympic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos enviados por el formulario de registro (login.tsx pestaña "Regístrate" -> login.html).
 * El rol NUNCA viaja desde el cliente: todo registro público queda como CLIENTE.
 */
public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres.")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido (ej: usuario@dominio.com).")
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, message = "Mínimo 6 caracteres.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).+$",
            message = "La contraseña debe tener mínimo 6 caracteres, una mayúscula, una minúscula, un número y un carácter especial."
    )
    private String password;

    public RegistroRequest() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
