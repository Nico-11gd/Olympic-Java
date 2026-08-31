function toggleTema() {
    var root = document.documentElement;
    var actual = root.getAttribute('data-theme');
    if (actual === 'light') {
        root.removeAttribute('data-theme');
        localStorage.removeItem('ol_tema');
    } else {
        root.setAttribute('data-theme', 'light');
        localStorage.setItem('ol_tema', 'light');
    }
    actualizarIconoTema();
}

function actualizarIconoTema() {
    var esClaro = document.documentElement.getAttribute('data-theme') === 'light';

    var iconoNav = document.getElementById('temaIcono');
    if (iconoNav) iconoNav.className = esClaro ? 'bi bi-sun-fill' : 'bi bi-moon-stars';

    var badgeAdmin = document.getElementById('temaEstadoAdmin');
    if (badgeAdmin) badgeAdmin.textContent = esClaro ? 'Claro' : 'Oscuro';

    var iconoAdmin = document.getElementById('temaIconoAdmin');
    if (iconoAdmin) iconoAdmin.className = esClaro ? 'bi bi-sun-fill' : 'bi bi-moon-stars';
}

function aplicarTemaGuardado() {
    if (localStorage.getItem('ol_tema') === 'light') {
        document.documentElement.setAttribute('data-theme', 'light');
    }
    actualizarIconoTema();
}

document.addEventListener('DOMContentLoaded', aplicarTemaGuardado);
