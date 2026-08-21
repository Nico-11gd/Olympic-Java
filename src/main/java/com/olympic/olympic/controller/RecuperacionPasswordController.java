package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ApiResponse;
import com.olympic.olympic.dto.CambiarPasswordRequest;
import com.olympic.olympic.dto.SolicitarCodigoRequest;
import com.olympic.olympic.dto.ValidarCodigoRequest;
import com.olympic.olympic.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flujo de "olvidé mi contraseña": solicitar código -> validar código -> cambiar contraseña.
 * Vive bajo /api/auth/** (ya público en SecurityConfig), no requiere cambios ahí.
 */
@RestController
@RequestMapping("/api/auth/recuperar")
public class RecuperacionPasswordController {

    private final PasswordResetService passwordResetService;

    public RecuperacionPasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/solicitar")
    public ResponseEntity<ApiResponse<Void>> solicitar(@Valid @RequestBody SolicitarCodigoRequest request) {
        passwordResetService.solicitarCodigo(request.getCorreo());
        return ResponseEntity.ok(ApiResponse.ok(
                "Si el correo está registrado, te enviamos un código de recuperación.", null));
    }

    @PostMapping("/validar")
    public ResponseEntity<ApiResponse<Void>> validar(@Valid @RequestBody ValidarCodigoRequest request) {
        passwordResetService.validarCodigo(request.getCorreo(), request.getCodigo());
        return ResponseEntity.ok(ApiResponse.ok("Código válido.", null));
    }

    @PostMapping("/cambiar")
    public ResponseEntity<ApiResponse<Void>> cambiar(@Valid @RequestBody CambiarPasswordRequest request) {
        passwordResetService.cambiarPassword(request.getCorreo(), request.getCodigo(), request.getNuevaPassword());
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente.", null));
    }
}