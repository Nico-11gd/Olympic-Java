package com.olympic.olympic.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Código de recuperación de contraseña.
 * Mapea la tabla `password_resets` que ya existía en la base de datos
 * (fk_reset_usuario, idx_token) — no se crea tabla nueva.
 */
@Entity
@Table(name = "password_resets")
public class PasswordReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "token", nullable = false, length = 10)
    private String token;

    @Column(name = "expira_at", nullable = false)
    private LocalDateTime expiraAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public PasswordReset() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public LocalDateTime getExpiraAt() {
        return expiraAt;
    }

    public void setExpiraAt(LocalDateTime expiraAt) {
        this.expiraAt = expiraAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}