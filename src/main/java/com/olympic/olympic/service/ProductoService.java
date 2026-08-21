package com.olympic.olympic.service;

import com.olympic.olympic.dto.ProductoRequest;
import com.olympic.olympic.dto.ProductoResponse;

import java.util.List;

public interface ProductoService {

    /** true → todos los productos (uso administrativo). false → solo activos con stock > 0 (catálogo público). */
    List<ProductoResponse> listar(boolean todos);

    ProductoResponse obtenerPorId(Integer id);

    ProductoResponse crear(ProductoRequest request);

    ProductoResponse actualizar(Integer id, ProductoRequest request);

    ProductoResponse cambiarEstado(Integer id, boolean activo);
}
