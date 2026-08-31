package com.olympic.olympic.dto;

import com.olympic.olympic.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos para actualizar un usuario existente (nombre, correo y rol).
 * Equivalente a Usuario::actualizar() en model/Usuario.php.
 */
public class UsuarioRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres.")
    private String nombre;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = "Ingresa un correo válido (ej: usuario@dominio.com).")
    private String correo;

    @NotNull(message = "El rol es obligatorio.")
    private Rol rol;

    public UsuarioRequest() {
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

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
