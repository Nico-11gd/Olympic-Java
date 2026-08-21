package com.olympic.olympic.service;

import com.olympic.olympic.dto.LoginRequest;
import com.olympic.olympic.dto.LoginResponse;
import com.olympic.olympic.dto.RegistroRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void registrar(RegistroRequest request);
}
