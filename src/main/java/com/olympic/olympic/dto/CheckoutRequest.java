package com.olympic.olympic.dto;

import com.olympic.olympic.entity.MetodoPago;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos que el cliente llena en el checkout: información de envío y método
 * de pago. Nombre y correo se autocompletan desde la sesión del usuario.
 */
public class CheckoutRequest {

    @NotBlank(message = "El nombre del destinatario es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    private String nombreEnvio;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Size(max = 20, message = "El teléfono no puede superar 20 caracteres.")
    private String telefono;

    @NotBlank(message = "El departamento es obligatorio.")
    @Size(max = 100, message = "El departamento no puede superar 100 caracteres.")
    private String departamento;

    @NotBlank(message = "La ciudad es obligatoria.")
    @Size(max = 100, message = "La ciudad no puede superar 100 caracteres.")
    private String ciudad;

    @NotBlank(message = "La dirección es obligatoria.")
    @Size(max = 255, message = "La dirección no puede superar 255 caracteres.")
    private String direccion;

    @Size(max = 255, message = "Las referencias no pueden superar 255 caracteres.")
    private String referencias;

    @NotNull(message = "Selecciona un método de pago.")
    private MetodoPago metodo;

    public CheckoutRequest() {
    }

    public String getNombreEnvio() {
        return nombreEnvio;
    }

    public void setNombreEnvio(String nombreEnvio) {
        this.nombreEnvio = nombreEnvio;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getReferencias() {
        return referencias;
    }

    public void setReferencias(String referencias) {
        this.referencias = referencias;
    }

    public MetodoPago getMetodo() {
        return metodo;
    }

    public void setMetodo(MetodoPago metodo) {
        this.metodo = metodo;
    }
}