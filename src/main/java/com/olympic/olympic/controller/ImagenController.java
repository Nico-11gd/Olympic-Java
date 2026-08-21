package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ApiResponse;
import com.olympic.olympic.dto.ImagenSubidaResponse;
import com.olympic.olympic.service.ImagenProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Equivalente Java de api/subir-imagen.php. Solo ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/api/imagenes")
public class ImagenController {

    private final ImagenProductoService imagenProductoService;

    public ImagenController(ImagenProductoService imagenProductoService) {
        this.imagenProductoService = imagenProductoService;
    }

    @PostMapping("/productos")
    public ResponseEntity<ApiResponse<ImagenSubidaResponse>> subir(@RequestParam("imagen") MultipartFile imagen) {
        ImagenSubidaResponse resultado = imagenProductoService.subir(imagen);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }
}
