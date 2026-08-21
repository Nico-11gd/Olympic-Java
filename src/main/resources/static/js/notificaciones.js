// ============================================================
// static/js/notificaciones.js
// Equivalente web de hooks/useNotification.ts: un toast reutilizable
// para mostrarError / mostrarExito / mostrarAdvertencia.
// Requiere un <div id="toast" class="toast" hidden></div> en la página.
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    var toastEl = null;
    var toastTimer = null;

    function obtenerToast() {
        if (!toastEl) {
            toastEl = document.getElementById('toast');
        }
        return toastEl;
    }

    function mostrarToast(mensaje, tipo) {
        var el = obtenerToast();
        if (!el) return;
        el.textContent = mensaje;
        el.className = 'toast ' + (tipo || 'exito');
        el.hidden = false;
        clearTimeout(toastTimer);
        toastTimer = setTimeout(function () { el.hidden = true; }, 4000);
    }

    window.Olympic.mostrarExito = function (mensaje) { mostrarToast(mensaje, 'exito'); };
    window.Olympic.mostrarError = function (mensaje) { mostrarToast(mensaje, 'error'); };
    window.Olympic.mostrarAdvertencia = function (mensaje) { mostrarToast(mensaje, 'advertencia'); };
})();
