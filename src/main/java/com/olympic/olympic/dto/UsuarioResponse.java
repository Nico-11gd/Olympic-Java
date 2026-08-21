package com.olympic.olympic.dto;

import com.olympic.olympic.entity.Rol;
import com.olympic.olympic.entity.Usuario;

import java.time.LocalDateTime;

/**
 * Representación pública de un Usuario. NUNCA incluye la contraseña.
 */
public class UsuarioResponse {

    private Integer id;
    private String nombre;
    private String correo;
    private Rol rol;
    private Boolean activo;
    private LocalDateTime createdAt;

    public UsuarioResponse() {
    }

    public static UsuarioResponse fromEntity(Usuario usuario) {
        UsuarioResponse dto = new UsuarioResponse();
        dto.setId(usuario.getId());
        dto.setNombre(usuario.getNombre());
        dto.setCorreo(usuario.getCorreo());
        dto.setRol(usuario.getRol());
        dto.setActivo(usuario.getActivo());
        dto.setCreatedAt(usuario.getCreatedAt());
        return dto;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
