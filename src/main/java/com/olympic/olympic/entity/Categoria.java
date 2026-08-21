package com.olympic.olympic.entity;

import jakarta.persistence.*;

/**
 * Tabla de apoyo para Productos (no es un módulo administrable en esta etapa,
 * solo se expone en modo lectura para poblar el selector de categoría).
 */
@Entity
@Table(name = "categorias")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Convert(converter = TipoTallaConverter.class)
    @Column(name = "tipo_talla", nullable = false)
    private TipoTalla tipoTalla = TipoTalla.NINGUNA;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    public Categoria() {
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoTalla getTipoTalla() {
        return tipoTalla;
    }

    public void setTipoTalla(TipoTalla tipoTalla) {
        this.tipoTalla = tipoTalla;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
