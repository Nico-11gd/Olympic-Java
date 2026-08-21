// ============================================================
// static/js/inicio.js
// Página pública de la tienda — equivalente a app/(tabs)/catalogo.tsx.
// El login solo se exige al comprar (ver producto.js).
// ============================================================
(function () {
    'use strict';

    var Estado = {
        productos: [],
        categorias: [],
        buscar: '',
        categoriaId: null,
        orden: 'recientes',
    };

    function el(id) { return document.getElementById(id); }

    function formatearPrecio(n) {
        return '$' + Math.round(n || 0).toLocaleString();
    }

    /** Equivalente a utils/precios.ts::calcularPrecio(), sobre el DTO Java (camelCase). */
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

    var Api = {
        cargar: function () {
            return Promise.all([
                window.Olympic.apiFetch('/api/productos'),
                window.Olympic.apiFetch('/api/categorias'),
            ]).then(function (respuestas) {
                Estado.productos = respuestas[0].body.success ? respuestas[0].body.data : [];
                Estado.categorias = respuestas[1].body.success ? respuestas[1].body.data : [];
            });
        },
    };

    function productosVisibles() {
        var buscar = Estado.buscar.trim().toLowerCase();

        return Estado.productos
            .filter(function (p) {
                var matchBuscar = !buscar || p.nombre.toLowerCase().indexOf(buscar) !== -1;
                var matchCategoria = !Estado.categoriaId || String(p.categoriaId) === Estado.categoriaId;
                return matchBuscar && matchCategoria;
            })
            .sort(function (a, b) {
                if (Estado.orden === 'precio_asc')  return Number(a.precio) - Number(b.precio);
                if (Estado.orden === 'precio_desc') return Number(b.precio) - Number(a.precio);
                if (Estado.orden === 'nombre')      return a.nombre.localeCompare(b.nombre);
                return Number(b.id) - Number(a.id); // recientes
            });
    }

    var Render = {

        navbar: function (usuario) {
            el('nav-login').hidden = !!usuario;
            el('nav-usuario').hidden = !usuario;
            if (usuario) el('nav-usuario-nombre').textContent = usuario.nombre;
        },

        chips: function () {
            var contenedor = el('ini-chips');
            contenedor.innerHTML = '';
            contenedor.appendChild(chip(null, 'Todos'));
            Estado.categorias.forEach(function (cat) {
                contenedor.appendChild(chip(String(cat.id), cat.nombre));
            });
        },

        titulo: function (lista) {
            var categoriaActiva = Estado.categoriaId
                ? Estado.categorias.filter(function (c) { return String(c.id) === Estado.categoriaId; })[0]
                : null;

            el('ini-titulo').textContent = categoriaActiva ? categoriaActiva.nombre : 'Todos los productos';
            el('ini-conteo').textContent = lista.length + (lista.length === 1 ? ' producto' : ' productos');
        },

        grid: function (lista) {
            var tpl = el('tpl-card');
            var contenedor = el('ini-grid');
            contenedor.innerHTML = '';

            lista.forEach(function (p) {
                var nodo = tpl.content.cloneNode(true);
                var info = calcularPrecio(p);

                nodo.querySelector('a.producto-card').href = '/producto?id=' + p.id;

                var img = nodo.querySelector('img.producto-img');
                var placeholder = nodo.querySelector('.img-placeholder');
                if (p.imagenUrl) {
                    img.src = p.imagenUrl;
                    img.hidden = false;
                    placeholder.hidden = true;
                }

                var badge = nodo.querySelector('.badge-descuento');
                if (info.tieneDescuento) {
                    badge.textContent = '-' + info.porcentaje + '%';
                    badge.hidden = false;
                }

                nodo.querySelector('.nombre-producto').textContent = p.nombre;
                nodo.querySelector('.precio').textContent = formatearPrecio(info.precioFinal);
                if (info.tieneDescuento) {
                    var precioAnterior = nodo.querySelector('.precio-anterior');
                    precioAnterior.textContent = formatearPrecio(info.precioAnterior);
                    precioAnterior.hidden = false;
                }

                contenedor.appendChild(nodo);
            });
        },

        todo: function () {
            var lista = productosVisibles();
            Render.titulo(lista);

            el('ini-vacio').hidden = lista.length > 0;
            el('ini-grid').hidden = lista.length === 0;
            if (lista.length > 0) Render.grid(lista);
        },
    };

    function chip(id, nombre) {
        var btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'ini-chip' + (Estado.categoriaId === id ? ' activo' : '');
        btn.textContent = nombre;
        btn.addEventListener('click', function () {
            Estado.categoriaId = id;
            Render.chips();
            Render.todo();
        });
        return btn;
    }

    function initEventos() {
        el('ini-buscar').addEventListener('input', function (e) {
            Estado.buscar = e.target.value;
            Render.todo();
        });

        el('ini-orden').addEventListener('change', function (e) {
            Estado.orden = e.target.value;
            Render.todo();
        });

        el('nav-logout').addEventListener('click', function () {
            window.Olympic.Auth.logout();
        });

        el('nav-carrito-btn').addEventListener('click', function () {
            window.Olympic.mostrarAdvertencia('El carrito estará disponible pronto');
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        initEventos();

        // Solo cambia el navbar, nunca bloquea la página. Si ves "Cerrar
        // sesión" sin haber iniciado sesión en esta pestaña, es que ya
        // tenías una cookie de sesión activa de antes.
        window.Olympic.Auth.me().then(Render.navbar);

        el('ini-cargando').hidden = false;
        Api.cargar()
            .catch(function () {
                window.Olympic.mostrarError('No se pudo cargar el catálogo');
            })
            .finally(function () {
                el('ini-cargando').hidden = true;
                Render.chips();
                Render.todo();
            });
    });
})();