// ============================================================
// static/js/auth.js
// Módulo central de sesión/autenticación, reutilizable por
// cualquier panel (ADMIN, CLIENTE y los que vengan después).
//
// El JWT vive en una cookie HttpOnly (la maneja el navegador
// solo, JS no puede leerla ni necesita hacerlo). Este módulo NO
// decide permisos — eso ya lo hace Spring Security en el backend.
// Aquí solo se resuelve la experiencia de navegación: a dónde
// mandar al usuario y cómo cerrar sesión.
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    var cache = null; // evita pedir /api/auth/me más de una vez por carga de página

    /** Usuario actual ({id, nombre, correo, rol}) o null si no hay sesión. */
    function me() {
        if (cache) return Promise.resolve(cache);
        return fetch('/api/auth/me')
            .then(function (res) {
                if (!res.ok) return null;
                return res.json().then(function (data) { return data.data; });
            })
            .then(function (usuario) {
                cache = usuario;
                return usuario;
            })
            .catch(function () { return null; });
    }

    /**
     * Exige sesión activa con el rol indicado (ej. "ADMIN", "CLIENTE").
     * Si no hay sesión o el rol no coincide, redirige a /login.
     * Uso típico en cada panel:
     *   Olympic.Auth.requireRole('ADMIN').then(function (usuario) { ... });
     */
    function requireRole(rolEsperado) {
        return me().then(function (usuario) {
            if (!usuario || String(usuario.rol).toUpperCase() !== rolEsperado.toUpperCase()) {
                window.location.href = '/login';
                return null;
            }
            return usuario;
        });
    }

    /** Cierra sesión en el servidor (borra la cookie) y redirige a /login. */
    function logout() {
        cache = null;
        fetch('/api/auth/logout', { method: 'POST' }).finally(function () {
            window.location.href = '/login';
        });
    }

    window.Olympic.Auth = {
        me: me,
        requireRole: requireRole,
        logout: logout
    };
})();
