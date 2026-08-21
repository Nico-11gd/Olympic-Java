package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ApiResponse;
import com.olympic.olympic.dto.LoginRequest;
import com.olympic.olympic.dto.LoginResponse;
import com.olympic.olympic.dto.RegistroRequest;
import com.olympic.olympic.dto.UsuarioResponse;
import com.olympic.olympic.repository.UsuarioRepository;
import com.olympic.olympic.security.JwtService;
import com.olympic.olympic.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Equivalente Java de api/login.php y api/registro.php.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public AuthController(AuthService authService, JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    // login.php -> POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        LoginResponse respuesta = authService.login(request);
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.crearCookie(respuesta.getToken()).toString());
        return ResponseEntity.ok(
                ApiResponse.ok("Bienvenido " + respuesta.getUsuario().getNombre(), respuesta));
    }

    // registro.php -> POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> registrar(@Valid @RequestBody RegistroRequest request) {
        authService.registrar(request);
        return ResponseEntity.ok(ApiResponse.ok("Usuario registrado correctamente", null));
    }

    // Cierra sesión de verdad: expira la cookie del JWT en el servidor
    // (no basta con que el frontend "olvide" el token).
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, jwtService.crearCookieExpirada().toString());
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada", null));
    }

    // Usado por auth.js (frontend) para saber quién es el usuario actual y su
    // rol. Hace falta porque el JWT ahora vive en una cookie HttpOnly que
    // JavaScript no puede leer directamente.
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> yo(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("No hay sesión activa."));
        }
        return usuarioRepository.findByCorreo(authentication.getName())
                .map(usuario -> ResponseEntity.ok(ApiResponse.ok(UsuarioResponse.fromEntity(usuario))))
                .orElseGet(() -> ResponseEntity.status(401).body(ApiResponse.error("No hay sesión activa.")));
    }
}
