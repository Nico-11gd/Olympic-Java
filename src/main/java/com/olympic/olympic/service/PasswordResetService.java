package com.olympic.olympic.service;

public interface PasswordResetService {

    void solicitarCodigo(String correo);

    void validarCodigo(String correo, String codigo);

    void cambiarPassword(String correo, String codigo, String nuevaPassword);
}