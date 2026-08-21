package com.olympic.olympic.exception;

public class CodigoInvalidoException extends RuntimeException {
    public CodigoInvalidoException(String mensaje) {
        super(mensaje);
    }
}