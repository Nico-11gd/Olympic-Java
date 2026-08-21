package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ApiResponse;
import com.olympic.olympic.dto.EstadoRequest;
import com.olympic.olympic.dto.ProductoRequest;
import com.olympic.olympic.dto.ProductoResponse;
import com.olympic.olympic.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Equivalente Java de api/productos.php. El acceso de escritura (POST/PUT/PATCH)
 * está restringido a ADMIN desde SecurityConfig; GET requiere solo estar autenticado.
 */
@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // productos.php?todos=1 -> GET /api/productos?todos=true (solo ADMIN ve inactivos/sin stock)
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductoResponse>>> listar(
            @RequestParam(name = "todos", required = false, defaultValue = "false") boolean todos,
            Authentication authentication
    ) {
        boolean puedeVerTodos = todos && esAdmin(authentication);
        return ResponseEntity.ok(ApiResponse.ok(productoService.listar(puedeVerTodos)));
    }

    // productos.php?id=X -> GET /api/productos/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(ApiResponse.ok(productoService.obtenerPorId(id)));
    }

    // POST productos.php -> POST /api/productos
    @PostMapping
    public ResponseEntity<ApiResponse<ProductoResponse>> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        return ResponseEntity.ok(ApiResponse.ok("Producto creado", creado));
    }

    // PUT productos.php (edición completa) -> PUT /api/productos/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductoResponse>> actualizar(
            @PathVariable Integer id, @Valid @RequestBody ProductoRequest request) {
        ProductoResponse actualizado = productoService.actualizar(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Producto actualizado", actualizado));
    }

    // PUT productos.php { id, activo } -> PATCH /api/productos/{id}/estado (activar/desactivar sin eliminar)
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<ProductoResponse>> cambiarEstado(
            @PathVariable Integer id, @Valid @RequestBody EstadoRequest request) {
        ProductoResponse actualizado = productoService.cambiarEstado(id, request.getActivo());
        String mensaje = Boolean.TRUE.equals(request.getActivo()) ? "Producto activado" : "Producto desactivado";
        return ResponseEntity.ok(ApiResponse.ok(mensaje, actualizado));
    }

    private boolean esAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(authority.getAuthority())) {
                return true;
            }
        }
        return false;
    }
}
