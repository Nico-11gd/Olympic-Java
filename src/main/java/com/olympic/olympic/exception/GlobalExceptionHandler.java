package com.olympic.olympic.exception;

import com.olympic.olympic.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * Manejo global de excepciones para toda la API REST (/api/**).
 * Devuelve siempre el mismo formato de respuesta que el resto del sistema:
 * { "success": false, "mensaje": "..." }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Validaciones @Valid en DTOs (Jakarta Validation) ──
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidacion(MethodArgumentNotValidException ex) {
        FieldError primerError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String mensaje = primerError != null ? primerError.getDefaultMessage() : "Datos inválidos.";
        return ResponseEntity.badRequest().body(ApiResponse.error(mensaje));
    }

    // ── Recurso no encontrado (usuario, producto...) ──
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(ex.getMessage()));
    }

    // ── Correo o código duplicado ──
    @ExceptionHandler(RecursoDuplicadoException.class)
    public ResponseEntity<ApiResponse<Void>> handleDuplicado(RecursoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(ex.getMessage()));
    }

    // ── Credenciales incorrectas ──
    @ExceptionHandler({ CredencialesInvalidasException.class, BadCredentialsException.class })
    public ResponseEntity<ApiResponse<Void>> handleCredenciales(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Correo o contraseña incorrectos"));
    }

    // ── Usuario inactivo ──
    @ExceptionHandler(UsuarioInactivoException.class)
    public ResponseEntity<ApiResponse<Void>> handleInactivo(UsuarioInactivoException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(ex.getMessage()));
    }

    // ── Código de recuperación inválido, usado o expirado ──
    @ExceptionHandler(CodigoInvalidoException.class)
    public ResponseEntity<ApiResponse<Void>> handleCodigoInvalido(CodigoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(ex.getMessage()));
    }

    // ── Fallo al enviar el correo de recuperación ──
    @ExceptionHandler(CorreoNoEnviadoException.class)
    public ResponseEntity<ApiResponse<Void>> handleCorreoNoEnviado(CorreoNoEnviadoException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("No pudimos enviar el correo. Intenta más tarde."));
    }

    // ── Imagen inválida / demasiado grande ──
    @ExceptionHandler(ImagenInvalidaException.class)
    public ResponseEntity<ApiResponse<Void>> handleImagen(ImagenInvalidaException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleImagenGrande(MaxUploadSizeExceededException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("La imagen no debe superar 5MB"));
    }

    // ── Acceso no autorizado (rol insuficiente) ──
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("No tienes permiso para realizar esta acción."));
    }

    // ── Argumentos ilegales (ej. rol desconocido) ──
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error(ex.getMessage()));
    }

    // ── Cualquier otro error no controlado ──
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error interno del servidor. Por favor, intenta más tarde."));
    }
}