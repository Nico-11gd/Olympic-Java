// ============================================================
// static/js/producto.js
// Detalle de producto — público. El login solo se exige al
// hacer clic en "Añadir al carrito".
//
// El cálculo de precios vive en precios.js, la tarjeta de "también te
// puede gustar" en producto-card.js y el navbar en navbar-publico.js —
// todo compartido también con inicio.js, nada duplicado aquí.
// ============================================================
(function () {
    'use strict';

    var Estado = {
        producto: null,
        tallaSeleccionada: null,
        colorSeleccionado: null,
        cantidad: 1,
    };

    function el(id) { return document.getElementById(id); }

    function idDesdeUrl() {
        return new URLSearchParams(window.location.search).get('id');
    }

    var Api = {
        cargar: function (id) {
            return window.Olympic.apiFetch('/api/productos/' + id).then(function (r) {
                if (!r.body.success) throw new Error(r.body.mensaje || 'Producto no encontrado');
                Estado.producto = r.body.data;
            });
        },
        cargarRelacionados: function (producto) {
            return window.Olympic.apiFetch('/api/productos').then(function (r) {
                if (!r.body.success) return [];
                return r.body.data
                    .filter(function (p) { return p.id !== producto.id && p.categoriaId === producto.categoriaId; })
                    .slice(0, 4);
            });
        },
    };

    var Render = {

        producto: function () {
            var p = Estado.producto;
            var info = window.Olympic.calcularPrecio(p);

            document.title = p.nombre + ' — OLIMPIC';

            if (p.imagenUrl) {
                el('pd-imagen').src = p.imagenUrl;
            } else {
                el('pd-imagen-wrap').classList.add('sin-imagen');
            }

            el('pd-categoria').textContent = p.categoria || '';
            el('pd-nombre').textContent = p.nombre;
            el('pd-precio').textContent = window.Olympic.formatearPrecio(info.precioFinal);

            if (info.tieneDescuento) {
                el('pd-precio-anterior').textContent = window.Olympic.formatearPrecio(info.precioAnterior);
                el('pd-precio-anterior').hidden = false;
                el('pd-badge-descuento').textContent = '-' + info.porcentaje + '%';
                el('pd-badge-descuento').hidden = false;
            }

            Render.opciones('pd-tallas-wrap', 'pd-tallas', p.tallas, 'talla');
            Render.opciones('pd-colores-wrap', 'pd-colores', p.colores, 'color');

            Render.stock();
            el('pd-descripcion').textContent = p.descripcion || '';
            el('pd-descripcion-wrap').hidden = !p.descripcion;
        },

        opciones: function (wrapId, listId, valores, tipo) {
            if (!valores || valores.length === 0) return;
            el(wrapId).hidden = false;
            var contenedor = el(listId);
            contenedor.innerHTML = '';
            valores.forEach(function (v) {
                var btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'pd-opcion-btn';
                btn.textContent = v;
                btn.addEventListener('click', function () {
                    if (tipo === 'talla') Estado.tallaSeleccionada = v; else Estado.colorSeleccionado = v;
                    Array.prototype.forEach.call(contenedor.children, function (b) { b.classList.remove('activo'); });
                    btn.classList.add('activo');
                });
                contenedor.appendChild(btn);
            });
        },

        stock: function () {
            var p = Estado.producto;
            var texto = el('pd-stock');
            if (p.stock === 0) {
                texto.textContent = 'Agotado';
                texto.className = 'pd-stock pd-stock-agotado';
                el('pd-btn-comprar').disabled = true;
            } else if (p.stock <= 5) {
                texto.textContent = 'Últimas ' + p.stock + ' unidades';
                texto.className = 'pd-stock pd-stock-bajo';
            } else {
                texto.textContent = 'En stock';
                texto.className = 'pd-stock pd-stock-ok';
            }
        },

        relacionados: function (lista) {
            if (lista.length === 0) return;
            el('pd-relacionados-wrap').hidden = false;
            var contenedor = el('pd-relacionados');
            contenedor.innerHTML = '';
            lista.forEach(function (p) { contenedor.appendChild(window.Olympic.crearTarjetaProducto(p)); });
        },
    };

    function cambiarCantidad(delta) {
        var max = Estado.producto.stock;
        var nueva = Estado.cantidad + delta;
        if (nueva < 1 || nueva > max) return;
        Estado.cantidad = nueva;
        el('pd-cant-valor').textContent = nueva;
    }

    function alAgregarCarrito() {
        var p = Estado.producto;

        if (p.tallas && p.tallas.length > 0 && !Estado.tallaSeleccionada) {
            window.Olympic.mostrarAdvertencia('Selecciona una talla');
            return;
        }
        if (p.colores && p.colores.length > 0 && !Estado.colorSeleccionado) {
            window.Olympic.mostrarAdvertencia('Selecciona un color');
            return;
        }

        window.Olympic.Auth.me().then(function (usuario) {
            if (!usuario) {
                window.location.href = '/login';
                return;
            }
            window.Olympic.mostrarAdvertencia('El carrito estará disponible muy pronto');
        });
    }

    function initEventos() {
        el('pd-cant-menos').addEventListener('click', function () { cambiarCantidad(-1); });
        el('pd-cant-mas').addEventListener('click', function () { cambiarCantidad(1); });
        el('pd-btn-comprar').addEventListener('click', alAgregarCarrito);
    }

    document.addEventListener('DOMContentLoaded', function () {
        var id = idDesdeUrl();
        if (!id) { window.location.href = '/'; return; }

        initEventos();
        window.Olympic.NavbarPublico.init();

        el('pd-cargando').hidden = false;
        Api.cargar(id)
            .then(function () {
                Render.producto();
                el('pd-contenido').hidden = false;
                return Api.cargarRelacionados(Estado.producto);
            })
            .then(Render.relacionados)
            .catch(function () {
                window.Olympic.mostrarError('No se pudo cargar el producto');
                setTimeout(function () { window.location.href = '/'; }, 1500);
            })
            .finally(function () {
                el('pd-cargando').hidden = true;
            });
    });
})();