// ============================================================
// static/js/api.js
// Envoltorio de fetch() para llamar a la API propia. La sesión
// viaja sola en una cookie HttpOnly (ver auth.js) — el navegador
// la adjunta automáticamente, no hay que armar ningún header
// de Authorization a mano.
// ============================================================
window.Olympic = window.Olympic || {};

(function () {
    'use strict';

    /**
     * Para peticiones JSON, pasa options.json (objeto) y se serializa solo;
     * para FormData (subida de imágenes) pasa options.body ya armado y NO
     * se agrega Content-Type (el navegador arma el boundary del multipart
     * automáticamente). Devuelve siempre el body ya parseado como JSON.
     */
    function apiFetch(url, options) {
        options = options || {};
        var headers = Object.assign({}, options.headers || {});

        var body = options.body;
        if (options.json !== undefined) {
            headers['Content-Type'] = 'application/json';
            body = JSON.stringify(options.json);
        }

        return fetch(url, {
            method: options.method || 'GET',
            headers: headers,
            body: body
        }).then(function (res) {
            if (res.status === 401) {
                window.Olympic.Auth.logout();
                return Promise.reject(new Error('Sesión expirada'));
            }
            return res.json().then(function (data) {
                return { status: res.status, ok: res.ok, body: data };
            });
        });
    }

    window.Olympic.apiFetch = apiFetch;
})();
