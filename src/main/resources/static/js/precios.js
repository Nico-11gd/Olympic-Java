// ============================================================
// static/js/precios.js
// Cálculo y formato de precios con promoción — antes duplicado
// (idéntico) en inicio.js y producto.js. Cualquier pantalla que
// muestre productos con precio (panel admin incluido) puede usar
// esto en vez de reescribirlo.
//
// Uso: Olympic.calcularPrecio(producto) / Olympic.formatearPrecio(n)
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    function formatearPrecio(n) {
        return '$' + Math.round(n || 0).toLocaleString();
    }

    /**
     * @param {{precio:number, precioPromocion?:number|null}} producto
     * @returns {{tieneDescuento:boolean, precioFinal:number, precioAnterior:number|null, porcentaje:number}}
     */
    function calcularPrecio(producto) {
        var tieneDescuento = producto.precioPromocion != null;
        var precioBase = Number(producto.precio);
        var precioFinal = tieneDescuento ? Number(producto.precioPromocion) : precioBase;

        return {
            tieneDescuento: tieneDescuento,
            precioFinal: precioFinal,
            precioAnterior: tieneDescuento ? precioBase : null,
            porcentaje: tieneDescuento ? Math.round((1 - precioFinal / precioBase) * 100) : 0,
        };
    }

    window.Olympic.formatearPrecio = formatearPrecio;
    window.Olympic.calcularPrecio = calcularPrecio;
})();