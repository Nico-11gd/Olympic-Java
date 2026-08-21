package com.olympic.olympic.service.impl;

import com.olympic.olympic.dto.ProductoRequest;
import com.olympic.olympic.dto.ProductoResponse;
import com.olympic.olympic.entity.Categoria;
import com.olympic.olympic.entity.Producto;
import com.olympic.olympic.entity.Promocion;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.repository.CategoriaRepository;
import com.olympic.olympic.repository.ProductoRepository;
import com.olympic.olympic.repository.PromocionRepository;
import com.olympic.olympic.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Lógica de negocio de Productos, equivalente a model/Producto.php.
 *
 * Diferencia deliberada respecto al PHP original: al editar, si se deja la
 * categoría vacía, aquí se guarda como NULL (la FK productos.categoria_id
 * permite NULL con ON DELETE SET NULL) en vez de enviar 0, que rompería la
 * restricción de clave foránea.
 */
@Service
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PromocionRepository promocionRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            PromocionRepository promocionRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.promocionRepository = promocionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean todos) {
        List<Producto> productos = todos
                ? productoRepository.findAllByOrderByIdDesc()
                : productoRepository.findByActivoTrueAndStockGreaterThanOrderByIdDesc(0);

        return productos.stream().map(ProductoResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponse obtenerPorId(Integer id) {
        return ProductoResponse.fromEntity(buscarPorId(id));
    }

    @Override
    @Transactional
    public ProductoResponse crear(ProductoRequest request) {
        if (request.getCategoriaId() == null) {
            throw new IllegalArgumentException("Selecciona una categoría.");
        }

        Producto producto = new Producto();
        producto.setCreatedAt(LocalDateTime.now());
        aplicarCambios(producto, request, true);

        return ProductoResponse.fromEntity(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponse actualizar(Integer id, ProductoRequest request) {
        Producto producto = buscarPorId(id);
        aplicarCambios(producto, request, false);
        return ProductoResponse.fromEntity(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponse cambiarEstado(Integer id, boolean activo) {
        Producto producto = buscarPorId(id);
        producto.setActivo(activo);
        return ProductoResponse.fromEntity(productoRepository.save(producto));
    }

    private void aplicarCambios(Producto producto, ProductoRequest request, boolean esCreacion) {
        String codigo = normalizarCodigo(request.getCodigo());
        validarCodigoUnico(codigo, producto.getId());

        producto.setNombre(request.getNombre().trim());
        producto.setCodigo(codigo);
        producto.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setImagen(request.getImagen());
        producto.setTalla(request.getTalla());
        producto.setColor(request.getColor());
        producto.setActivo(request.getActivo() != null ? request.getActivo() : (esCreacion ? Boolean.TRUE : producto.getActivo()));
        producto.setCategoria(resolverCategoria(request.getCategoriaId()));
        producto.setPromocion(resolverPromocion(request.getPromocionId()));
    }

    private Categoria resolverCategoria(Integer categoriaId) {
        if (categoriaId == null || categoriaId == 0) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
    }

    private Promocion resolverPromocion(Integer promocionId) {
        if (promocionId == null || promocionId == 0) {
            return null;
        }
        return promocionRepository.findById(promocionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Promoción no encontrada"));
    }

    private String normalizarCodigo(String codigo) {
        return (codigo != null && !codigo.isBlank()) ? codigo.trim() : null;
    }

    private void validarCodigoUnico(String codigo, Integer idActual) {
        if (codigo == null) {
            return;
        }
        boolean existe = (idActual == null)
                ? productoRepository.existsByCodigo(codigo)
                : productoRepository.existsByCodigoAndIdNot(codigo, idActual);

        if (existe) {
            throw new RecursoDuplicadoException("Ya existe un producto con ese código");
        }
    }

    private Producto buscarPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado"));
    }
}
