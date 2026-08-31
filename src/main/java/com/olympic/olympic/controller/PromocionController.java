package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ApiResponse;
import com.olympic.olympic.dto.PromocionResponse;
import com.olympic.olympic.repository.PromocionRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Solo lectura: alimenta el selector de promoción del formulario de productos.
 * Por alcance de esta etapa NO se implementa un módulo de promociones completo.
 */
@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionRepository promocionRepository;

    public PromocionController(PromocionRepository promocionRepository) {
        this.promocionRepository = promocionRepository;
    }

    @GetMapping
    public ApiResponse<List<PromocionResponse>> listar() {
        List<PromocionResponse> promociones = promocionRepository.findByActivoTrueOrderByIdDesc()
                .stream().map(PromocionResponse::fromEntity).toList();
        return ApiResponse.ok(promociones);
    }
}
