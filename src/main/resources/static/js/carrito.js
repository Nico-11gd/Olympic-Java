document.addEventListener('submit', function (e) {
    var t = e.target;
    if (!t || !t.action || t.action.indexOf('/carrito/') === -1) return;
    e.preventDefault();
    var overlay = document.getElementById('carrito');
    fetch(t.action, { method: 'POST', body: new FormData(t), headers: { 'X-Requested-With': 'XMLHttpRequest' } })
        .then(function (r) {
            var contador = r.headers.get('X-Carrito-Contador');
            return r.text().then(function (html) {
                if (overlay) overlay.outerHTML = html;
                actualizarBadge(contador);
                if (location.hash !== '#carrito') history.pushState(null, '', '#carrito');
            });
        });
});

function actualizarBadge(contador) {
    var badge = document.getElementById('carritoBadge');
    if (!contador || Number(contador) <= 0) { if (badge) badge.style.display = 'none'; return; }
    if (!badge) {
        var a = document.querySelector('a[href="#carrito"]');
        if (!a) return;
        badge = document.createElement('span');
        badge.id = 'carritoBadge';
        badge.className = 'position-absolute top-0 start-100 translate-middle badge rounded-pill';
        badge.style.cssText = 'background:var(--ol-gold);color:var(--ol-black);font-size:.6rem;';
        a.appendChild(badge);
    }
    badge.textContent = contador;
    badge.style.display = '';
}
