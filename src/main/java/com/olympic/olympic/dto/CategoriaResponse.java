package com.olympic.olympic.dto;

import com.olympic.olympic.entity.Categoria;
import com.olympic.olympic.entity.TipoTalla;

public class CategoriaResponse {

    private Integer id;
    private String nombre;
    private String descripcion;
    private TipoTalla tipoTalla;

    public static CategoriaResponse fromEntity(Categoria categoria) {
        CategoriaResponse dto = new CategoriaResponse();
        dto.id = categoria.getId();
        dto.nombre = categoria.getNombre();
        dto.descripcion = categoria.getDescripcion();
        dto.tipoTalla = categoria.getTipoTalla();
        return dto;
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
}
