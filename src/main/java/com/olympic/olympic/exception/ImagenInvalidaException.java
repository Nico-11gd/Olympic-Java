package com.olympic.olympic.exception;

/** Formato de imagen no permitido o imagen demasiado grande. */
public class ImagenInvalidaException extends RuntimeException {
    public ImagenInvalidaException(String mensaje) {
        super(mensaje);
    }
}
