package com.olympic.olympic.dto;

import com.olympic.olympic.entity.Promocion;
import com.olympic.olympic.entity.TipoPromocion;

import java.math.BigDecimal;

public class PromocionResponse {

    private Integer id;
    private String nombre;
    private TipoPromocion tipo;
    private BigDecimal valor;

    public static PromocionResponse fromEntity(Promocion promocion) {
        PromocionResponse dto = new PromocionResponse();
        dto.id = promocion.getId();
        dto.nombre = promocion.getNombre();
        dto.tipo = promocion.getTipo();
        dto.valor = promocion.getValor();
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

    public TipoPromocion getTipo() {
        return tipo;
    }

    public void setTipo(TipoPromocion tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
