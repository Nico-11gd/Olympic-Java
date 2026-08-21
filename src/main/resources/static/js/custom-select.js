// ============================================================
// static/js/custom-select.js
// Dropdown personalizado que reemplaza visualmente los <select>
// nativos. El <select> original sigue siendo la fuente de verdad
// (value, evento 'change'), así toda la lógica existente en
// producto-form.js sigue funcionando sin cambios.
// Expone window.Olympic.CustomSelect.{mejorar, actualizar, cerrarTodas}
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    var instancias = {}; // selectId -> Instancia

    function crearChevron() {
        var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
        svg.setAttribute('viewBox', '0 0 24 24');
        svg.setAttribute('class', 'cs-chevron');
        var path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
        path.setAttribute('d', 'M6 9l6 6 6-6');
        svg.appendChild(path);
        return svg;
    }

    function leerOpciones(select) {
        return Array.prototype.map.call(select.options, function (opt) {
            return { value: opt.value, texto: opt.textContent, deshabilitada: opt.disabled };
        });
    }

    function Instancia(select) {
        this.select = select;
        this.abierto = false;

        var wrapper = select.closest('.campo-wrapper') || select.parentNode;
        select.classList.add('cs-select-oculto');

        this.trigger = document.createElement('button');
        this.trigger.type = 'button';
        this.trigger.className = 'cs-trigger';

        this.texto = document.createElement('span');
        this.texto.className = 'cs-trigger-texto';

        this.trigger.appendChild(this.texto);
        this.trigger.appendChild(crearChevron());

        this.panel = document.createElement('div');
        this.panel.className = 'cs-panel';
        this.panel.hidden = true;

        wrapper.appendChild(this.trigger);
        document.body.appendChild(this.panel);

        this._bind();
        this.render();
    }

    Instancia.prototype._bind = function () {
        var self = this;

        this.trigger.addEventListener('click', function (e) {
            e.stopPropagation();
            self.toggle();
        });

        document.addEventListener('click', function (e) {
            if (self.abierto && e.target !== self.trigger && !self.panel.contains(e.target)) {
                self.cerrar();
            }
        });

        document.addEventListener('keydown', function (e) {
            if (self.abierto && e.key === 'Escape') self.cerrar();
        });

        window.addEventListener('resize', function () { if (self.abierto) self.cerrar(); });
        document.addEventListener('scroll', function () { if (self.abierto) self.cerrar(); }, true);
    };

    Instancia.prototype.posicionar = function () {
        var rect = this.trigger.getBoundingClientRect();
        this.panel.style.left = rect.left + 'px';
        this.panel.style.top = (rect.bottom + 6) + 'px';
        this.panel.style.width = rect.width + 'px';
    };

    Instancia.prototype.abrir = function () {
        if (this.select.disabled || this.select.options.length === 0) return;
        cerrarTodas();
        this.posicionar();
        this.abierto = true;
        this.panel.hidden = false;
        this.trigger.classList.add('cs-trigger-abierto');
    };

    Instancia.prototype.cerrar = function () {
        this.abierto = false;
        this.panel.hidden = true;
        this.trigger.classList.remove('cs-trigger-abierto');
    };

    Instancia.prototype.toggle = function () {
        if (this.abierto) this.cerrar(); else this.abrir();
    };

    Instancia.prototype.seleccionar = function (valor) {
        if (this.select.value !== valor) {
            this.select.value = valor;
            this.render();
            this.select.dispatchEvent(new Event('change', { bubbles: true }));
        }
        this.cerrar();
    };

    Instancia.prototype.render = function () {
        var self = this;
        var opciones = leerOpciones(this.select);

        this.panel.innerHTML = '';
        opciones.forEach(function (op) {
            var item = document.createElement('button');
            item.type = 'button';
            item.className = 'cs-opcion';
            if (op.value === self.select.value) item.classList.add('cs-opcion-activa');
            item.disabled = op.deshabilitada;
            item.textContent = op.texto;
            item.addEventListener('click', function (e) {
                e.stopPropagation();
                self.seleccionar(op.value);
            });
            self.panel.appendChild(item);
        });

        var seleccionada = opciones.filter(function (op) { return op.value === self.select.value; })[0];
        this.texto.textContent = seleccionada ? seleccionada.texto : '';
        this.trigger.classList.toggle('cs-trigger-vacio', !this.select.value);
        this.trigger.disabled = this.select.disabled;
    };

    function cerrarTodas() {
        Object.keys(instancias).forEach(function (id) { instancias[id].cerrar(); });
    }

    function mejorar(selectId) {
        var select = document.getElementById(selectId);
        if (!select || instancias[selectId]) return;
        instancias[selectId] = new Instancia(select);
    }

    function actualizar(selectId) {
        var instancia = instancias[selectId];
        if (instancia) instancia.render();
    }

    window.Olympic.CustomSelect = { mejorar: mejorar, actualizar: actualizar, cerrarTodas: cerrarTodas };
})();