package com.olympic.olympic.exception;

/** Correo o contraseña incorrectos. */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
