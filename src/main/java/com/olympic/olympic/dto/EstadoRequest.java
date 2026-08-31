package com.olympic.olympic.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Body de los endpoints PATCH .../estado — activar / desactivar sin eliminar el registro.
 */
public class EstadoRequest {

    @NotNull(message = "El campo 'activo' es obligatorio.")
    private Boolean activo;

    public EstadoRequest() {
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
