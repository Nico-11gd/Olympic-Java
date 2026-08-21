package com.olympic.olympic.service;

public interface EmailService {

    void enviarCodigoRecuperacion(String destinatario, String nombreUsuario, String codigo, int expiracionMinutos);
}