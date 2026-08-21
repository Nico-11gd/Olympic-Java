package com.olympic.olympic.exception;

public class CorreoNoEnviadoException extends RuntimeException {
    public CorreoNoEnviadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}