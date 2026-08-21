// ============================================================
// static/js/producto-card.js
// Arma una tarjeta de producto a partir de fragments/producto-card.html
// (#tpl-card). Antes esta lógica estaba duplicada en inicio.js
// (Render.grid) y producto.js (Render.relacionados) — ahora ambas
// páginas llaman a Olympic.crearTarjetaProducto(producto).
//
// Depende de precios.js (Olympic.calcularPrecio / formatearPrecio).
// Reutilizable en cualquier pantalla que necesite mostrar productos
// en formato tarjeta (ej. un futuro listado de favoritos).
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    function crearTarjetaProducto(producto) {
        var tpl = document.getElementById('tpl-card');
        var nodo = tpl.content.cloneNode(true);
        var info = window.Olympic.calcularPrecio(producto);

        nodo.querySelector('a.producto-card').href = '/producto?id=' + producto.id;

        var contenedorImg = nodo.querySelector('.producto-card-img');
        var img = nodo.querySelector('img.producto-img');
        if (producto.imagenUrl) {
            img.src = producto.imagenUrl;
        } else {
            contenedorImg.classList.add('sin-imagen');
        }

        var badge = nodo.querySelector('.badge-descuento');
        if (info.tieneDescuento) {
            badge.textContent = '-' + info.porcentaje + '%';
            badge.hidden = false;
        }

        nodo.querySelector('.nombre-producto').textContent = producto.nombre;
        nodo.querySelector('.precio').textContent = window.Olympic.formatearPrecio(info.precioFinal);
        if (info.tieneDescuento) {
            var anterior = nodo.querySelector('.precio-anterior');
            anterior.textContent = window.Olympic.formatearPrecio(info.precioAnterior);
            anterior.hidden = false;
        }

        return nodo;
    }

    window.Olympic.crearTarjetaProducto = crearTarjetaProducto;
})();