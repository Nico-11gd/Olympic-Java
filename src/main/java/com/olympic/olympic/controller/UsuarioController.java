package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ApiResponse;
import com.olympic.olympic.dto.EstadoRequest;
import com.olympic.olympic.dto.UsuarioRequest;
import com.olympic.olympic.dto.UsuarioResponse;
import com.olympic.olympic.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de usuarios (solo ADMIN, ver SecurityConfig).
 * Equivalente Java de la parte de usuarios de AdminController.php / model/Usuario.php.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioResponse>>> obtenerTodos() {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtenerTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(usuarioService.obtenerPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody UsuarioRequest request) {
        UsuarioResponse actualizado = usuarioService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario actualizado", actualizado));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> cambiarEstado(
            @PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        UsuarioResponse actualizado = usuarioService.cambiarEstado(id, request.getActivo());
        String mensaje = Boolean.TRUE.equals(request.getActivo()) ? "Usuario activado" : "Usuario desactivado";
        return ResponseEntity.ok(ApiResponse.ok(mensaje, actualizado));
    }
}
