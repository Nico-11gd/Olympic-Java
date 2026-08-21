package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ApiResponse;
import com.olympic.olympic.dto.CategoriaResponse;
import com.olympic.olympic.repository.CategoriaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Solo lectura: alimenta el selector de categoría del formulario de productos.
 * Por alcance de esta etapa NO se implementa un módulo de categorías completo
 * (crear/editar/desactivar), tal como se solicitó.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public ApiResponse<List<CategoriaResponse>> listar() {
        List<CategoriaResponse> categorias = categoriaRepository.findByActivoTrueOrderByNombreAsc()
                .stream().map(CategoriaResponse::fromEntity).toList();
        return ApiResponse.ok(categorias);
    }
}
