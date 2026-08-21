// ============================================================
// static/js/productos.js
// Pantalla de listado — equivalente a app/admin/productos.tsx
// ============================================================
(function () {
    'use strict';

    var RED = '#E53935', GREEN = '#43A047';

    var productos = [];
    var filtroActivo = 'todos';
    var busqueda = '';

    function el(id) { return document.getElementById(id); }

    function rgba(hex, alpha) {
        var r = parseInt(hex.substr(1, 2), 16), g = parseInt(hex.substr(3, 2), 16), b = parseInt(hex.substr(5, 2), 16);
        return 'rgba(' + r + ',' + g + ',' + b + ',' + alpha + ')';
    }

    function formatearPrecio(n) {
        return '$' + Math.round(n || 0).toLocaleString();
    }

    // ── Carga ──
    function cargar() {
        el('p-cargando').hidden = false;
        el('p-tabla-wrap').hidden = true;
        el('p-cards').hidden = true;
        el('p-vacio').hidden = true;

        return window.Olympic.apiFetch('/api/productos?todos=true')
            .then(function (r) {
                if (r.body.success) {
                    productos = r.body.data;
                    render();
                } else {
                    window.Olympic.mostrarError(r.body.mensaje || 'No se pudieron cargar los productos');
                }
            })
            .catch(function () {
                window.Olympic.mostrarError('No se pudo conectar con el servidor.');
            })
            .finally(function () {
                el('p-cargando').hidden = true;
            });
    }

    // ── Filtro + búsqueda ──
    function productosFiltrados() {
        var q = busqueda.toLowerCase();
        return productos.filter(function (p) {
            var matchBusqueda =
                (p.nombre || '').toLowerCase().indexOf(q) !== -1 ||
                (p.categoria || '').toLowerCase().indexOf(q) !== -1 ||
                (p.codigo || '').toLowerCase().indexOf(q) !== -1;

            var matchFiltro =
                filtroActivo === 'todos' ? true :
                filtroActivo === 'activos' ? p.activo === true :
                filtroActivo === 'inactivos' ? p.activo === false :
                filtroActivo === 'bajo' ? p.stock <= 5 : true;

            return matchBusqueda && matchFiltro;
        });
    }

    // ── Render ──
    function render() {
        var lista = productosFiltrados();
        el('p-contador').textContent = lista.length + ' producto' + (lista.length !== 1 ? 's' : '');

        if (lista.length === 0) {
            el('p-tabla-wrap').hidden = true;
            el('p-cards').hidden = true;
            el('p-vacio').hidden = false;
            return;
        }

        el('p-vacio').hidden = true;
        el('p-tabla-wrap').hidden = false;
        el('p-cards').hidden = false;

        renderTabla(lista);
        renderCards(lista);
    }

    function aplicarComunes(nodo, p) {
        var imgEl = nodo.querySelector('.p-img, .p-img-movil');
        var placeholderEl = nodo.querySelector('.p-img-placeholder, .p-img-placeholder-movil');
        if (p.imagenUrl) {
            imgEl.onerror = function () {
                // El archivo no existe en el servidor (p. ej. no se migró la imagen
                // al nuevo directorio de uploads): mostramos el placeholder en vez
                // del icono de "imagen rota" del navegador.
                imgEl.hidden = true;
                placeholderEl.hidden = false;
            };
            imgEl.src = p.imagenUrl;
            imgEl.hidden = false;
            placeholderEl.hidden = true;
        } else {
            imgEl.hidden = true;
            placeholderEl.hidden = false;
        }

        nodo.querySelector('.p-nombre').textContent = p.nombre;
        var codigoEl = nodo.querySelector('.p-codigo');
        codigoEl.textContent = p.codigo || '';
        codigoEl.hidden = !p.codigo;

        var categoriaEl = nodo.querySelector('.p-categoria');
        if (categoriaEl) categoriaEl.textContent = p.categoria || '—';

        nodo.querySelector('.p-precio').textContent = formatearPrecio(p.precio);

        var esBajoStock = p.stock <= 5;
        var stockBadge = nodo.querySelector('.p-stock-badge');
        stockBadge.textContent = nodo.classList.contains('p-card-producto') ? ('Stock: ' + p.stock) : String(p.stock);
        stockBadge.style.background = esBajoStock ? rgba(RED, 0.08) : rgba(GREEN, 0.07);
        stockBadge.style.color = esBajoStock ? RED : GREEN;

        var estadoColor = p.activo ? GREEN : RED;
        nodo.querySelector('.p-estado-badge').style.background = rgba(estadoColor, 0.09);
        nodo.querySelector('.p-estado-dot').style.background = estadoColor;
        var estadoTexto = nodo.querySelector('.p-estado-texto');
        estadoTexto.textContent = p.activo ? 'Activo' : 'Inactivo';
        estadoTexto.style.color = estadoColor;

        var btnToggle = nodo.querySelector('.p-btn-toggle');
        btnToggle.style.background = p.activo ? rgba('#F57C00', 0.08) : rgba(GREEN, 0.08);
        btnToggle.querySelector('use').setAttribute('href', p.activo ? '#icon-eye-off' : '#icon-eye');
        btnToggle.style.color = p.activo ? '#F57C00' : GREEN;
        btnToggle.addEventListener('click', function () { cambiarEstado(p.id, !p.activo); });

        var btnEditar = nodo.querySelector('.p-btn-editar');
        btnEditar.style.background = rgba('#1565C0', 0.08);
        btnEditar.style.color = '#1565C0';
        btnEditar.addEventListener('click', function () { window.Olympic.ProductoForm.abrirEditar(p.id); });
    }

    function renderTabla(lista) {
        var body = el('p-tabla-body');
        var tpl = el('tpl-fila');
        body.innerHTML = '';
        lista.forEach(function (p) {
            var nodo = tpl.content.firstElementChild.cloneNode(true);
            aplicarComunes(nodo, p);
            body.appendChild(nodo);
        });
    }

    function renderCards(lista) {
        var contenedor = el('p-cards');
        var tpl = el('tpl-card');
        contenedor.innerHTML = '';
        lista.forEach(function (p) {
            var nodo = tpl.content.firstElementChild.cloneNode(true);
            aplicarComunes(nodo, p);
            contenedor.appendChild(nodo);
        });
    }

    // ── Cambiar estado ──
    function cambiarEstado(id, nuevoActivo) {
        window.Olympic.apiFetch('/api/productos/' + id + '/estado', { method: 'PATCH', json: { activo: nuevoActivo } })
            .then(function (r) {
                if (r.body.success) {
                    var p = productos.find(function (x) { return x.id === id; });
                    if (p) p.activo = nuevoActivo;
                    window.Olympic.mostrarExito((p ? p.nombre : 'Producto') + (nuevoActivo ? ' activado' : ' desactivado'));
                    render();
                } else {
                    window.Olympic.mostrarError(r.body.mensaje || 'No se pudo actualizar');
                }
            })
            .catch(function () {
                window.Olympic.mostrarError('No se pudo conectar con el servidor.');
            });
    }

    // ── Filtros / búsqueda / eventos de página ──
    function initEventos() {
        document.querySelectorAll('.p-filtro').forEach(function (btn) {
            btn.addEventListener('click', function () {
                document.querySelectorAll('.p-filtro').forEach(function (b) { b.classList.remove('activo'); });
                btn.classList.add('activo');
                filtroActivo = btn.dataset.filtro;
                render();
            });
        });

        var buscador = el('p-buscar');
        var btnLimpiar = el('p-buscar-limpiar');
        buscador.addEventListener('input', function () {
            busqueda = buscador.value;
            btnLimpiar.hidden = busqueda.length === 0;
            render();
        });
        btnLimpiar.addEventListener('click', function () {
            buscador.value = '';
            busqueda = '';
            btnLimpiar.hidden = true;
            render();
        });

        el('btn-nuevo-producto').addEventListener('click', function () {
            window.Olympic.ProductoForm.abrirCrear();
        });

        el('btn-cerrar-sesion').addEventListener('click', function () {
            window.Olympic.Auth.logout();
        });
    }

    document.addEventListener('DOMContentLoaded', function () {
        window.Olympic.Auth.requireRole('ADMIN').then(function (usuario) {
            if (!usuario) return; // ya fue redirigido a /login dentro de requireRole

            window.Olympic.ProductoForm.init({ onGuardado: cargar });
            initEventos();
            cargar();
        });
    });
})();
