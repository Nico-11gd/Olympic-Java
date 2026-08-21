package com.olympic.olympic.dto;

import com.olympic.olympic.entity.Producto;
import com.olympic.olympic.entity.TipoPromocion;
import com.olympic.olympic.entity.TipoTalla;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Representación pública de un Producto, equivalente a la fila que arma
 * model/Producto.php::queryConPromocion() (incluye datos de categoría,
 * promoción y el precio ya calculado con descuento).
 */
public class ProductoResponse {

    /** Debe coincidir con el prefijo mapeado en WebConfig (recursos estáticos de imágenes). */
    private static final String PREFIJO_IMAGEN = "/uploads/";

    private Integer id;
    private String nombre;
    private String codigo;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String imagen;
    private String imagenUrl;
    private String color;
    private String talla;
    private Boolean activo;
    private LocalDateTime createdAt;

    private Integer categoriaId;
    private String categoria;
    private TipoTalla categoriaTipoTalla;

    private Integer promocionId;
    private String promocionNombre;
    private TipoPromocion promocionTipo;
    private BigDecimal promocionValor;

    /** Precio final tras aplicar la promoción activa, o null si no aplica (igual que el CASE WHEN del PHP original). */
    private BigDecimal precioPromocion;

    public static ProductoResponse fromEntity(Producto p) {
        ProductoResponse dto = new ProductoResponse();
        dto.id = p.getId();
        dto.nombre = p.getNombre();
        dto.codigo = p.getCodigo();
        dto.descripcion = p.getDescripcion();
        dto.precio = p.getPrecio();
        dto.stock = p.getStock();
        dto.imagen = p.getImagen();
        dto.imagenUrl = (p.getImagen() != null && !p.getImagen().isBlank())
                ? PREFIJO_IMAGEN + p.getImagen()
                : null;
        dto.color = p.getColor();
        dto.talla = p.getTalla();
        dto.activo = p.getActivo();
        dto.createdAt = p.getCreatedAt();

        if (p.getCategoria() != null) {
            dto.categoriaId = p.getCategoria().getId();
            dto.categoria = p.getCategoria().getNombre();
            dto.categoriaTipoTalla = p.getCategoria().getTipoTalla();
        }

        if (p.getPromocion() != null) {
            dto.promocionId = p.getPromocion().getId();
            dto.promocionNombre = p.getPromocion().getNombre();
            dto.promocionTipo = p.getPromocion().getTipo();
            dto.promocionValor = p.getPromocion().getValor();
            dto.precioPromocion = calcularPrecioPromocion(p);
        }

        return dto;
    }

    // Replica exacta del CASE WHEN de queryConPromocion() en model/Producto.php:
    // solo exige promocion.activo = 1 y tipo válido; NO valida fecha_inicio/fecha_fin
    // (el proyecto original tampoco lo hace).
    private static BigDecimal calcularPrecioPromocion(Producto p) {
        if (p.getPromocion() == null || !Boolean.TRUE.equals(p.getPromocion().getActivo())) {
            return null;
        }
        BigDecimal precio = p.getPrecio();
        BigDecimal valor = p.getPromocion().getValor();

        if (p.getPromocion().getTipo() == TipoPromocion.PORCENTAJE) {
            BigDecimal descuento = precio.multiply(valor).divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            return precio.subtract(descuento).setScale(2, RoundingMode.HALF_UP);
        } else if (p.getPromocion().getTipo() == TipoPromocion.VALOR_FIJO) {
            return precio.subtract(valor).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    /** Tallas ya separadas en lista (para el frontend, equivalente a p.talla.split(',') en el TSX). */
    public List<String> getTallas() {
        return dividir(talla);
    }

    /** Colores ya separados en lista. */
    public List<String> getColores() {
        return dividir(color);
    }

    private static List<String> dividir(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return Arrays.stream(texto.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getImagenUrl() {
        return imagenUrl;
    }

    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public TipoTalla getCategoriaTipoTalla() {
        return categoriaTipoTalla;
    }

    public void setCategoriaTipoTalla(TipoTalla categoriaTipoTalla) {
        this.categoriaTipoTalla = categoriaTipoTalla;
    }

    public Integer getPromocionId() {
        return promocionId;
    }

    public void setPromocionId(Integer promocionId) {
        this.promocionId = promocionId;
    }

    public String getPromocionNombre() {
        return promocionNombre;
    }

    public void setPromocionNombre(String promocionNombre) {
        this.promocionNombre = promocionNombre;
    }

    public TipoPromocion getPromocionTipo() {
        return promocionTipo;
    }

    public void setPromocionTipo(TipoPromocion promocionTipo) {
        this.promocionTipo = promocionTipo;
    }

    public BigDecimal getPromocionValor() {
        return promocionValor;
    }

    public void setPromocionValor(BigDecimal promocionValor) {
        this.promocionValor = promocionValor;
    }

    public BigDecimal getPrecioPromocion() {
        return precioPromocion;
    }

    public void setPrecioPromocion(BigDecimal precioPromocion) {
        this.precioPromocion = precioPromocion;
    }
}
