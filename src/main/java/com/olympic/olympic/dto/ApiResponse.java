package com.olympic.olympic.dto;

/**
 * Envoltorio de respuesta estándar para toda la API.
 * Mantiene la misma forma que las respuestas de la API PHP original:
 * { "success": true|false, "mensaje": "...", "data": ... }
 * de modo que el frontend (Thymeleaf/JS) pueda reutilizar la misma lógica
 * de manejo de respuestas que ya existía en el proyecto TSX (res.data.success / res.data.mensaje).
 */
public class ApiResponse<T> {

    private boolean success;
    private String mensaje;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String mensaje, T data) {
        this.success = success;
        this.mensaje = mensaje;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ApiResponse<T> ok(String mensaje, T data) {
        return new ApiResponse<>(true, mensaje, data);
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(false, mensaje, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
