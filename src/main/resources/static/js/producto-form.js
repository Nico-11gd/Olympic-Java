// ============================================================
// static/js/producto-form.js
// Lógica compartida del modal Crear/Editar producto — equivalente a
// _crear-producto.tsx + _editar-producto.tsx (comparten casi toda la lógica).
// Expone window.Olympic.ProductoForm.{init, abrirCrear, abrirEditar}
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    var SUGERENCIAS_TALLA_ROPA = ['XS', 'S', 'M', 'L', 'XL', 'XXL'];
    var SUGERENCIAS_TALLA_CALZADO = ['36', '37', '38', '39', '40', '41', '42', '43', '44'];

    var modo = 'crear'; // 'crear' | 'editar'
    var productoIdActual = null;
    var categorias = [];
    var promociones = [];
    var onGuardadoCallback = null;

    var selectorTallas, selectorColores;

    // ── Utilidades ──
    function formatearPrecio(n) {
        return '$' + Math.round(n || 0).toLocaleString();
    }

    function el(id) { return document.getElementById(id); }

    function setCampoError(prefix, mensaje) {
        var wrapper = el(prefix + '-wrapper');
        var existente = el(prefix + '-error');
        if (mensaje) {
            wrapper.classList.add('error');
            if (!existente) {
                existente = document.createElement('p');
                existente.id = prefix + '-error';
                existente.className = 'campo-error';
                wrapper.insertAdjacentElement('afterend', existente);
            }
            existente.innerHTML = '<svg class="icon icon-xs"><use href="#icon-warning"></use></svg><span>' + mensaje + '</span>';
            existente.hidden = false;
        } else {
            wrapper.classList.remove('error');
            if (existente) existente.hidden = true;
        }
    }

    // ── Selector de etiquetas reutilizable (tallas / colores) ──
    function crearSelectorEtiquetas(containerId, opciones) {
        var container = el(containerId);
        var valores = (opciones.valoresIniciales || []).slice();
        var sugerencias = opciones.sugerencias || [];
        var ayuda = opciones.ayuda || '';

        container.innerHTML =
            '<div class="etiquetas-input">' +
            '  <svg class="icon icon-muted icon-sm"><use href="#icon-' + (opciones.icono || 'pricetag') + '"></use></svg>' +
            '  <input type="text" placeholder="Escribe y presiona Enter" />' +
            '  <button type="button"><svg class="icon icon-sm"><use href="#icon-plus"></use></svg></button>' +
            '</div>' +
            '<div class="etiquetas-sugerencias"></div>' +
            '<div class="etiquetas-chips"></div>' +
            '<p class="etiquetas-ayuda"></p>';

        var input = container.querySelector('input');
        var btnAgregar = container.querySelector('button');
        var sugerenciasEl = container.querySelector('.etiquetas-sugerencias');
        var chipsEl = container.querySelector('.etiquetas-chips');
        var ayudaEl = container.querySelector('.etiquetas-ayuda');

        function agregar(valor) {
            var limpio = (valor || '').trim();
            if (!limpio) return;
            var yaExiste = valores.some(function (v) { return v.toLowerCase() === limpio.toLowerCase(); });
            if (!yaExiste) valores.push(limpio);
            input.value = '';
            render();
        }

        function quitar(valor) {
            valores = valores.filter(function (v) { return v !== valor; });
            render();
        }

        function render() {
            btnAgregar.classList.toggle('activo', input.value.trim().length > 0);

            sugerenciasEl.innerHTML = '';
            sugerencias
                .filter(function (s) { return !valores.some(function (v) { return v.toLowerCase() === s.toLowerCase(); }); })
                .forEach(function (s) {
                    var chip = document.createElement('button');
                    chip.type = 'button';
                    chip.className = 'etiqueta-sugerencia';
                    chip.textContent = '+ ' + s;
                    chip.addEventListener('click', function () { agregar(s); });
                    sugerenciasEl.appendChild(chip);
                });

            chipsEl.innerHTML = '';
            valores.forEach(function (v) {
                var chip = document.createElement('span');
                chip.className = 'etiqueta-chip';
                var texto = document.createElement('span');
                texto.textContent = v;
                var btn = document.createElement('button');
                btn.type = 'button';
                btn.innerHTML = '<svg class="icon icon-xs"><use href="#icon-close"></use></svg>';
                btn.addEventListener('click', function () { quitar(v); });
                chip.appendChild(texto);
                chip.appendChild(btn);
                chipsEl.appendChild(chip);
            });

            ayudaEl.textContent = (ayuda && valores.length === 0) ? ayuda : '';
        }

        input.addEventListener('input', render);
        input.addEventListener('keydown', function (e) { if (e.key === 'Enter') { e.preventDefault(); agregar(input.value); } });
        btnAgregar.addEventListener('click', function () { agregar(input.value); });

        render();

        return {
            get: function () { return valores.slice(); },
            set: function (nuevos) { valores = (nuevos || []).slice(); render(); },
            setSugerencias: function (nuevas) { sugerencias = nuevas || []; render(); },
        };
    }

    // ── Carga de categorías / promociones ──
    function cargarListas() {
        return Promise.all([
            window.Olympic.apiFetch('/api/categorias'),
            window.Olympic.apiFetch('/api/promociones'),
        ]).then(function (respuestas) {
            categorias = respuestas[0].body.success ? respuestas[0].body.data : [];
            promociones = respuestas[1].body.success ? respuestas[1].body.data : [];
            poblarSelectCategorias();
            poblarSelectPromociones();
        });
    }

    function poblarSelectCategorias() {
        var select = el('pf-categoria');
        var actual = select.value;
        select.innerHTML = '<option value="">— Seleccionar categoría —</option>';
        categorias.forEach(function (c) {
            var opt = document.createElement('option');
            opt.value = c.id;
            opt.textContent = c.nombre;
            select.appendChild(opt);
        });
        select.value = actual;
        window.Olympic.CustomSelect.actualizar('pf-categoria');
    }

    function poblarSelectPromociones() {
        var select = el('pf-promocion');
        var actual = select.value;
        select.innerHTML = '<option value="">— Sin promoción —</option>';
        promociones.forEach(function (p) {
            var opt = document.createElement('option');
            opt.value = p.id;
            var etiqueta = p.tipo === 'porcentaje' ? ('-' + p.valor + '%') : ('-' + formatearPrecio(p.valor));
            opt.textContent = p.nombre + ' (' + etiqueta + ')';
            select.appendChild(opt);
        });
        select.value = actual;
        window.Olympic.CustomSelect.actualizar('pf-promocion');
    }

    function categoriaSeleccionada() {
        var id = el('pf-categoria').value;
        return categorias.find(function (c) { return String(c.id) === String(id); });
    }

    function promocionSeleccionada() {
        var id = el('pf-promocion').value;
        return promociones.find(function (p) { return String(p.id) === String(id); });
    }

    // ── Tallas: mostrar/ocultar según tipo_talla de la categoría ──
    function actualizarTallasSegunCategoria() {
        var cat = categoriaSeleccionada();
        var tipo = cat ? cat.tipoTalla : 'NINGUNA';
        var tipoLower = (tipo || 'NINGUNA').toLowerCase();
        var wrap = el('pf-tallas-wrap');

        if (tipoLower === 'ropa' || tipoLower === 'calzado') {
            wrap.hidden = false;
            selectorTallas.setSugerencias(tipoLower === 'ropa' ? SUGERENCIAS_TALLA_ROPA : SUGERENCIAS_TALLA_CALZADO);
        } else {
            wrap.hidden = true;
        }
    }

    // ── Indicadores de stock ──
    function actualizarIndicadoresStock() {
        var stockVal = el('pf-stock').value;
        var stockNum = parseInt(stockVal || '0', 10);
        var esBajo = stockVal !== '' && stockNum > 0 && stockNum <= 5;
        var esCero = stockVal !== '' && stockNum === 0;

        el('pf-alerta-stock').hidden = !esBajo;
        el('pf-stock-ind').hidden = !esCero;
        if (esCero) el('pf-stock-ind-texto').textContent = 'Sin stock disponible';
    }

    // ── Preview de descuento ──
    function actualizarPreviewDescuento() {
        var promo = promocionSeleccionada();
        var precio = parseFloat(el('pf-precio').value);
        var preview = el('pf-preview-descuento');

        if (!promo || isNaN(precio) || precio <= 0) { preview.hidden = true; return; }

        var precioFinal = promo.tipo === 'porcentaje'
            ? precio - (precio * promo.valor / 100)
            : Math.max(precio - promo.valor, 0);

        el('pf-preview-descuento-texto').innerHTML =
            'Precio con descuento: <strong>' + formatearPrecio(precioFinal) + '</strong> (antes ' + formatearPrecio(precio) + ')';
        preview.hidden = false;
    }

    // ── Imagen ──
    function mostrarImagen(url) {
        var placeholder = el('pf-imagen-placeholder');
        var img = el('pf-imagen-img');
        var overlay = el('pf-imagen-overlay');
        if (url) {
            img.src = url;
            img.hidden = false;
            placeholder.hidden = true;
            overlay.hidden = false;
        } else {
            img.hidden = true;
            placeholder.hidden = false;
            overlay.hidden = true;
        }
    }

    function subirImagen(archivo) {
        el('pf-imagen-cargando').hidden = false;
        var formData = new FormData();
        formData.append('archivo', archivo);

        window.Olympic.apiFetch('/api/imagenes/productos', { method: 'POST', body: formData })
            .then(function (r) {
                if (r.body.success) {
                    el('pf-imagen-filename').value = r.body.data.nombreArchivo;
                    mostrarImagen(r.body.data.url);
                } else {
                    window.Olympic.mostrarError(r.body.mensaje || 'No se pudo subir la imagen');
                }
            })
            .catch(function () { window.Olympic.mostrarError('Error de conexión al subir la imagen'); })
            .finally(function () { el('pf-imagen-cargando').hidden = true; });
    }

    // ── Reset / poblado del formulario ──
    function resetFormulario() {
        el('pf-form').reset();
        el('pf-id').value = '';
        el('pf-imagen-filename').value = '';
        mostrarImagen(null);
        ['pf-nombre', 'pf-precio', 'pf-stock', 'pf-codigo'].forEach(function (p) { setCampoError(p, null); });
        el('pf-alerta-stock').hidden = true;
        el('pf-stock-ind').hidden = true;
        el('pf-preview-descuento').hidden = true;
        el('pf-tallas-wrap').hidden = true;
        selectorTallas.set([]);
        selectorColores.set([]);
        window.Olympic.CustomSelect.actualizar('pf-categoria');
        window.Olympic.CustomSelect.actualizar('pf-estado');
        window.Olympic.CustomSelect.actualizar('pf-promocion');
    }

    function poblarFormulario(p) {
        el('pf-id').value = p.id;
        el('pf-nombre').value = p.nombre || '';
        el('pf-codigo').value = p.codigo || '';
        el('pf-descripcion').value = p.descripcion || '';
        el('pf-precio').value = p.precio != null ? p.precio : '';
        el('pf-stock').value = p.stock != null ? p.stock : '';
        el('pf-estado').value = p.activo ? '1' : '0';
        el('pf-categoria').value = p.categoriaId != null ? p.categoriaId : '';
        el('pf-promocion').value = p.promocionId != null ? p.promocionId : '';
        window.Olympic.CustomSelect.actualizar('pf-estado');
        window.Olympic.CustomSelect.actualizar('pf-categoria');
        window.Olympic.CustomSelect.actualizar('pf-promocion');
        el('pf-imagen-filename').value = p.imagen || '';
        mostrarImagen(p.imagenUrl || null);
        selectorTallas.set(p.talla ? p.talla.split(',').map(function (t) { return t.trim(); }).filter(Boolean) : []);
        selectorColores.set(p.color ? p.color.split(',').map(function (c) { return c.trim(); }).filter(Boolean) : []);
        actualizarTallasSegunCategoria();
        actualizarIndicadoresStock();
        actualizarPreviewDescuento();
    }

    // ── Validación ──
    function validar() {
        var nombre = el('pf-nombre').value.trim();
        var precio = el('pf-precio').value;
        var stock = el('pf-stock').value;
        var categoriaId = el('pf-categoria').value;

        var errNombre = nombre === '' ? 'El nombre es obligatorio.' : (nombre.length < 2 ? 'El nombre debe tener al menos 2 caracteres.' : '');
        var errPrecio = precio === '' ? 'El precio es obligatorio.' : (isNaN(parseFloat(precio)) || parseFloat(precio) <= 0 ? 'Precio inválido.' : '');
        var errStock = stock === '' ? 'El stock es obligatorio.' : (isNaN(parseInt(stock, 10)) || parseInt(stock, 10) < 0 ? 'Stock inválido.' : '');
        var errCategoria = (modo === 'crear' && categoriaId === '') ? 'Selecciona una categoría.' : '';

        setCampoError('pf-nombre', errNombre);
        setCampoError('pf-precio', errPrecio);
        setCampoError('pf-stock', errStock);

        return !errNombre && !errPrecio && !errStock && !errCategoria;
    }

    // ── Envío ──
    function guardar(e) {
        e.preventDefault();
        if (!validar()) {
            window.Olympic.mostrarAdvertencia('Corrige los campos marcados.');
            return;
        }

        var payload = {
            nombre: el('pf-nombre').value.trim(),
            codigo: el('pf-codigo').value.trim(),
            descripcion: el('pf-descripcion').value.trim(),
            precio: parseFloat(el('pf-precio').value),
            stock: parseInt(el('pf-stock').value, 10),
            categoriaId: el('pf-categoria').value ? parseInt(el('pf-categoria').value, 10) : null,
            promocionId: el('pf-promocion').value ? parseInt(el('pf-promocion').value, 10) : null,
            imagen: el('pf-imagen-filename').value,
            activo: el('pf-estado').value === '1',
            talla: selectorTallas.get().join(','),
            color: selectorColores.get().join(','),
        };

        var boton = el('pf-btn-guardar');
        boton.disabled = true;
        boton.querySelector('.btn-texto').hidden = true;
        boton.querySelector('.spinner').hidden = false;

        var peticion = modo === 'crear'
            ? window.Olympic.apiFetch('/api/productos', { method: 'POST', json: payload })
            : window.Olympic.apiFetch('/api/productos/' + productoIdActual, { method: 'PUT', json: payload });

        peticion.then(function (r) {
            if (r.body.success) {
                window.Olympic.mostrarExito(modo === 'crear' ? 'Producto creado' : 'Producto actualizado');
                cerrar();
                if (onGuardadoCallback) onGuardadoCallback();
            } else {
                window.Olympic.mostrarError(r.body.mensaje || 'No se pudo guardar el producto');
            }
        }).catch(function () {
            window.Olympic.mostrarError('Error de conexión');
        }).finally(function () {
            boton.disabled = false;
            boton.querySelector('.btn-texto').hidden = false;
            boton.querySelector('.spinner').hidden = true;
        });
    }

    // ── Apertura / cierre ──
    function abrir() {
        el('pf-overlay').hidden = false;
    }

    function cerrar() {
        el('pf-overlay').hidden = true;
        window.Olympic.CustomSelect.cerrarTodas();
        resetFormulario();
    }

    function abrirCrear() {
        modo = 'crear';
        productoIdActual = null;
        resetFormulario();
        el('pf-titulo').textContent = 'Nuevo producto';
        el('pf-subtitulo').textContent = 'Completa todos los campos obligatorios';
        el('pf-subtitulo').hidden = false;
        el('pf-categoria-asterisco').hidden = false;
        el('pf-alerta-stock').hidden = true;
        el('pf-stock-ind').hidden = true;
        el('pf-btn-guardar-texto').textContent = 'Crear producto';
        el('pf-cargando').hidden = false;
        el('pf-form').hidden = true;
        abrir();

        cargarListas().finally(function () {
            el('pf-cargando').hidden = true;
            el('pf-form').hidden = false;
        });
    }

    function abrirEditar(id) {
        modo = 'editar';
        productoIdActual = id;
        resetFormulario();
        el('pf-titulo').textContent = 'Editar producto';
        el('pf-subtitulo').textContent = 'Modificando el producto #' + id;
        el('pf-subtitulo').hidden = false;
        el('pf-categoria-asterisco').hidden = true;
        el('pf-btn-guardar-texto').textContent = 'Guardar cambios';
        el('pf-cargando').hidden = false;
        el('pf-form').hidden = true;
        abrir();

        Promise.all([
            window.Olympic.apiFetch('/api/productos/' + id),
            cargarListas(),
        ]).then(function (respuestas) {
            var resProd = respuestas[0];
            if (resProd.body.success) {
                poblarFormulario(resProd.body.data);
            } else {
                window.Olympic.mostrarError(resProd.body.mensaje || 'No se pudo cargar el producto');
                cerrar();
            }
        }).catch(function () {
            window.Olympic.mostrarError('No se pudieron cargar los datos');
            cerrar();
        }).finally(function () {
            el('pf-cargando').hidden = true;
            el('pf-form').hidden = false;
        });
    }

    function init(opciones) {
        onGuardadoCallback = (opciones && opciones.onGuardado) || null;

        selectorTallas = crearSelectorEtiquetas('pf-tallas', {
            icono: 'resize', ayuda: 'Agrega las tallas que tienes en stock de este producto.',
        });
        selectorColores = crearSelectorEtiquetas('pf-colores', {
            icono: 'palette', ayuda: 'Agrega los colores que tienes en stock de este producto.',
        });

        window.Olympic.CustomSelect.mejorar('pf-categoria');
        window.Olympic.CustomSelect.mejorar('pf-estado');
        window.Olympic.CustomSelect.mejorar('pf-promocion');

        el('pf-cerrar').addEventListener('click', cerrar);
        el('pf-btn-cancelar').addEventListener('click', cerrar);
        el('pf-form').addEventListener('submit', guardar);

        el('pf-categoria').addEventListener('change', actualizarTallasSegunCategoria);
        el('pf-stock').addEventListener('input', actualizarIndicadoresStock);
        el('pf-precio').addEventListener('input', actualizarPreviewDescuento);
        el('pf-promocion').addEventListener('change', actualizarPreviewDescuento);

        el('pf-imagen-preview').addEventListener('click', function () { el('pf-imagen-input').click(); });
        el('pf-btn-seleccionar-imagen').addEventListener('click', function () { el('pf-imagen-input').click(); });
        el('pf-imagen-input').addEventListener('change', function (e) {
            if (e.target.files && e.target.files[0]) subirImagen(e.target.files[0]);
        });
    }

    window.Olympic.ProductoForm = { init: init, abrirCrear: abrirCrear, abrirEditar: abrirEditar };
})();