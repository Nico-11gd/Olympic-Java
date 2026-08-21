package com.olympic.olympic.exception;

/** Correo duplicado, código de producto duplicado, etc. */
public class RecursoDuplicadoException extends RuntimeException {
    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
