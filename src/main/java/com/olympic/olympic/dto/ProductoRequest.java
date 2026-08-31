package com.olympic.olympic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Datos para crear/actualizar un producto. Equivalente a los payloads que
 * arman _crear-producto.tsx / _editar-producto.tsx antes de llamar a productos.php.
 *
 * La categoría es obligatoria al CREAR (igual que en el formulario de creación)
 * pero opcional al EDITAR (igual que en el formulario de edición) — esa
 * asimetría se valida a mano en ProductoServiceImpl, no aquí con anotaciones,
 * porque ambos formularios comparten el mismo DTO.
 */
public class ProductoRequest {

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 2, message = "El nombre debe tener al menos 2 caracteres.")
    private String nombre;

    private String codigo;

    private String descripcion;

    @NotNull(message = "El precio es obligatorio.")
    @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0.")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio.")
    @PositiveOrZero(message = "El stock no puede ser negativo.")
    private Integer stock;

    private Integer categoriaId;

    private Integer promocionId;

    private String imagen;

    private Boolean activo;

    /** Tallas separadas por coma, ej: "S,M,L" (SelectorEtiquetas.tsx las envía ya unidas). */
    private String talla;

    /** Colores separados por coma, ej: "Negro,Blanco". */
    private String color;

    public ProductoRequest() {
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

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Integer getPromocionId() {
        return promocionId;
    }

    public void setPromocionId(Integer promocionId) {
        this.promocionId = promocionId;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
