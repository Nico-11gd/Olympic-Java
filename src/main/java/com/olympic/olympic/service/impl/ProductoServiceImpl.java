package com.olympic.olympic.service.impl;

import com.olympic.olympic.dto.ProductoRequest;
import com.olympic.olympic.dto.ProductoResponse;
import com.olympic.olympic.entity.Categoria;
import com.olympic.olympic.entity.Producto;
import com.olympic.olympic.entity.Promocion;
import com.olympic.olympic.entity.TipoPromocion;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.repository.CategoriaRepository;
import com.olympic.olympic.repository.ProductoRepository;
import com.olympic.olympic.repository.PromocionRepository;
import com.olympic.olympic.service.ProductoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            PromocionRepository promocionRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.promocionRepository = promocionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponse> listar(boolean todos) {
        List<Producto> productos = todos
                ? productoRepository.findAllByOrderByIdDesc()
                : productoRepository.findByActivoTrueOrderByNombreAsc();

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
        Categoria categoria = resolverCategoria(request.getCategoriaId());
        String codigo = resolverCodigo(request.getCodigo(), categoria, esCreacion, producto.getCodigo());
        validarCodigoUnico(codigo, producto.getId());

        producto.setNombre(request.getNombre().trim());
        producto.setCodigo(codigo);
        producto.setDescripcion(request.getDescripcion() != null ? request.getDescripcion().trim() : null);
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        // Solo se reemplaza la imagen si viene una nueva (subida en el controller);
        // si no se seleccionó archivo nuevo, se conserva la imagen actual del producto.
        if (request.getImagen() != null && !request.getImagen().isBlank()) {
            producto.setImagen(request.getImagen());
        }
        producto.setTalla(request.getTalla());
        producto.setColor(request.getColor());
        producto.setActivo(
                request.getActivo() != null ? request.getActivo() : (esCreacion ? Boolean.TRUE : producto.getActivo()));
        producto.setCategoria(categoria);

        Promocion promocion = resolverPromocion(request.getPromocionId());
        producto.setPromocion(promocion);
        producto.setOfferPrice(calcularPrecioFinal(promocion, request.getPrecio()));
    }

    /**
     * Calcula el precio final con descuento (columna offer_price) a partir de
     * la promoción seleccionada. Si no hay promoción (null) o no está activa,
     * devuelve null para que offer_price quede vacío en BD.
     */
    private BigDecimal calcularPrecioFinal(Promocion promocion, BigDecimal precio) {
        if (promocion == null || !Boolean.TRUE.equals(promocion.getActivo()) || precio == null) {
            return null;
        }
        BigDecimal valor = promocion.getValor();
        if (promocion.getTipo() == TipoPromocion.PORCENTAJE) {
            BigDecimal descuento = precio.multiply(valor)
                    .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);
            return precio.subtract(descuento).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        } else if (promocion.getTipo() == TipoPromocion.VALOR_FIJO) {
            return precio.subtract(valor).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private Promocion resolverPromocion(Integer promocionId) {
        if (promocionId == null || promocionId == 0) {
            return null;
        }
        return promocionRepository.findById(promocionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Promoción no encontrada"));
    }

    /**
     * Resuelve el código final del producto. Si viene uno en el formulario se
     * respeta (normalizado); si vino vacío se genera uno automático con el
     * prefijo de la categoría, o se conserva el actual al editar.
     */
    private String resolverCodigo(String codigoFormulario, Categoria categoria, boolean esCreacion, String codigoActual) {
        String manual = normalizarCodigo(codigoFormulario);
        if (manual != null) {
            return manual;
        }
        if (esCreacion && categoria != null) {
            return generarCodigo(categoria);
        }
        return codigoActual;
    }

    /**
     * Genera un código único del tipo PREFIJO-XXX (ej: CAM-002) a partir del
     * nombre de la categoría. Asigna el primer número secuencial libre
     * (001, 002, ...) dentro del prefijo para que los códigos queden ordenados.
     */
    private String generarCodigo(Categoria categoria) {
        String prefijo = prefijoCategoria(categoria);
        return String.format("%s-%03d", prefijo, siguienteNumeroLibre(prefijo));
    }

    /**
     * Devuelve el primer número de secuencia libre para un prefijo dado,
     * examinando los códigos existentes que lo usan.
     */
    private int siguienteNumeroLibre(String prefijo) {
        List<Integer> usados = productoRepository.findByCodigoStartingWith(prefijo)
                .stream().map(Producto::getCodigo).filter(java.util.Objects::nonNull)
                .map(codigo -> codigo.substring(prefijo.length()).replaceFirst("^-(\\d+).*$", "$1"))
                .filter(java.util.Objects::nonNull)
                .map(this::parseNumero)
                .collect(java.util.stream.Collectors.toList());
        for (int i = 1; ; i++) {
            if (!usados.contains(i)) {
                return i;
            }
        }
    }

    private Integer parseNumero(String texto) {
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException ignorado) {
            return -1;
        }
    }

    /**
     * Deriva el prefijo del SKU a partir del nombre de la categoría:
     * primeras letras alfanuméricas en mayúsculas y sin acentos.
     * Ej: "Camisetas" -> CAM, "Accesorios" -> ACC.
     */
    private String prefijoCategoria(Categoria categoria) {
        String limpio = java.text.Normalizer.normalize(categoria.getNombre(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9]", "");
        return limpio.isEmpty() ? "SKU" : limpio.substring(0, Math.min(3, limpio.length())).toUpperCase();
    }

    private Categoria resolverCategoria(Integer categoriaId) {
        if (categoriaId == null || categoriaId == 0) {
            return null;
        }
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Categoría no encontrada"));
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
