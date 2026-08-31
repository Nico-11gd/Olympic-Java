package com.olympic.olympic.service;

import com.olympic.olympic.dto.RegistroRequest;

/**
 * El login ya no vive aquí: lo resuelve Spring Security (formLogin +
 * CustomUserDetailsService) directamente sobre POST /login. Este servicio
 * solo se encarga del registro público de nuevos clientes.
 */
public interface AuthService {

    void registrar(RegistroRequest request);
}
