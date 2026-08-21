package com.olympic.olympic.exception;

/** El usuario existe y la contraseña es correcta, pero activo = 0. */
public class UsuarioInactivoException extends RuntimeException {
    public UsuarioInactivoException(String mensaje) {
        super(mensaje);
    }
}
