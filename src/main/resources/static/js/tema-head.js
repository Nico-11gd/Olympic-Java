(function () {
    try {
        if (localStorage.getItem('ol_tema') === 'light') {
            document.documentElement.setAttribute('data-theme', 'light');
        }
    } catch (e) {}
})();
