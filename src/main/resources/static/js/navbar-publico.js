// ============================================================
// static/js/navbar-publico.js
// Comportamiento de fragments/navbar-publico.html — antes copiado
// (idéntico) en inicio.js y producto.js: mostrar Ingresar/Cerrar sesión
// según haya sesión, y el aviso de "carrito próximamente".
//
// Uso: Olympic.NavbarPublico.init() — se puede llamar desde cualquier
// página pública que incluya el fragmento del navbar.
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    function el(id) { return document.getElementById(id); }

    function pintar(usuario) {
        el('nav-login').hidden = !!usuario;
        el('nav-usuario').hidden = !usuario;
        if (usuario) {
            el('nav-panel-admin').hidden = String(usuario.rol).toLowerCase() !== 'admin';
        }
        return usuario;
    }

    function abrirPanelLateral() {
        el('panel-lateral').hidden = false;
        el('panel-lateral-fondo').hidden = false;
    }

    function cerrarPanelLateral() {
        el('panel-lateral').hidden = true;
        el('panel-lateral-fondo').hidden = true;
    }

    function init() {
        el('nav-logout').addEventListener('click', function () {
            window.Olympic.Auth.logout();
        });
        el('nav-carrito-btn').addEventListener('click', function () {
            window.Olympic.mostrarAdvertencia('El carrito estará disponible pronto');
        });
        el('nav-panel-admin').addEventListener('click', abrirPanelLateral);
        el('panel-lateral-cerrar').addEventListener('click', cerrarPanelLateral);
        el('panel-lateral-fondo').addEventListener('click', cerrarPanelLateral);

        return window.Olympic.Auth.me().then(pintar);
    }

    window.Olympic.NavbarPublico = { init: init };
})();