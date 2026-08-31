package com.olympic.olympic.exception;

/** Usuario no encontrado, Producto no encontrado, etc. */
public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
