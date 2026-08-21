// ============================================================
// static/js/login.js
// Réplica del comportamiento de app/login.tsx:
// validaciones inline en tiempo real, cambio de pestaña, registro, login, JWT.
// ============================================================
(function () {
    'use strict';

    // ── Funciones de validación (reutilizadas en login, registro y recuperación) ──
    function validarCorreo(valor) {
        valor = valor || '';
        if (valor.trim() === '') return 'Los campos no pueden estar vacíos.';
        if (/\s/.test(valor)) return 'El correo no puede contener espacios.';

        var local = valor.split('@')[0];
        if (local.length > 0 && /^[^A-Za-z0-9]/.test(local)) {
            return 'El correo no puede comenzar con un carácter especial.';
        }

        if (/\.\./.test(valor)) return 'Ingresa un correo válido.';

        var regexCorreo = /^[A-Za-z0-9](?:[A-Za-z0-9._-]*[A-Za-z0-9])?@[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?)+$/;
        if (!regexCorreo.test(valor)) {
            return 'Ingresa un correo válido.';
        }

        return '';
    }

    function validarPassword(valor) {
        if (!valor) return 'Los campos no pueden estar vacíos.';
        if (valor.length < 6) return 'La contraseña debe tener al menos 6 caracteres.';
        return '';
    }

    function validarNombre(valor) {
        if (!valor || !valor.trim()) return 'Los campos no pueden estar vacíos.';
        if (valor.trim().length < 2) return 'El nombre debe tener al menos 2 caracteres.';
        return '';
    }

    function validarPasswordFuerte(valor) {
        if (!valor) return 'Los campos no pueden estar vacíos.';
        if (valor.length < 6) return 'Mínimo 6 caracteres.';
        if (!/[A-Z]/.test(valor)) return 'Debe tener al menos una letra mayúscula.';
        if (!/[a-z]/.test(valor)) return 'Debe tener al menos una letra minúscula.';
        if (!/[0-9]/.test(valor)) return 'Debe tener al menos un número.';
        if (!/[^A-Za-z0-9]/.test(valor)) return 'Debe tener al menos un carácter especial (!@#$%...).';
        return '';
    }

    // ── Toast (ver static/js/notificaciones.js) ──
    var mostrarToast = function (mensaje, tipo) {
        if (tipo === 'error') return window.Olympic.mostrarError(mensaje);
        if (tipo === 'advertencia') return window.Olympic.mostrarAdvertencia(mensaje);
        return window.Olympic.mostrarExito(mensaje);
    };

    // ── Helpers de campo (icono de error + ayuda) ──
    function setCampoEstado(prefix, ayudaTextoOriginal, error) {
        var wrapper = document.getElementById(prefix + '-wrapper');
        var ayuda = document.getElementById(prefix + '-ayuda');
        var existente = document.getElementById(prefix + '-error');

        if (error) {
            wrapper.classList.add('error');
            if (ayuda) ayuda.hidden = true;
            if (!existente) {
                existente = document.createElement('p');
                existente.id = prefix + '-error';
                existente.className = 'campo-error';
                wrapper.insertAdjacentElement('afterend', existente);
            }
            existente.innerHTML =
                '<svg class="icon icon-xs"><use href="#icon-warning"></use></svg><span>' + error + '</span>';
            existente.hidden = false;
        } else {
            wrapper.classList.remove('error');
            if (ayuda) ayuda.hidden = false;
            if (existente) existente.hidden = true;
        }
    }

    // ── Validación en tiempo real: input/change validan mientras se escribe ──
    // ── o se autocompleta; blur cubre el caso de campo vacío sin escribir;  ──
    // ── no se muestra ningún error hasta que el usuario interactúa.        ──
    function attachValidacionTiempoReal(input, prefijo, validador, alEscribir) {
        var tocado = false;

        function ejecutar() {
            if (!tocado) return;
            setCampoEstado(prefijo, null, validador(input.value));
        }

        input.addEventListener('focus', function () { tocado = true; });

        input.addEventListener('input', function () {
            tocado = true;
            if (typeof alEscribir === 'function') alEscribir();
            ejecutar();
        });

        // Cubre autocompletado del navegador / gestor de contraseñas,
        // que no siempre dispara 'input'.
        input.addEventListener('change', function () {
            tocado = true;
            if (typeof alEscribir === 'function') alEscribir();
            ejecutar();
        });

        input.addEventListener('animationstart', function (e) {
            if (e.animationName === 'olympicAutofillStart') {
                tocado = true;
                if (typeof alEscribir === 'function') alEscribir();
                ejecutar();
            }
        });

        input.addEventListener('blur', function () {
            tocado = true;
            ejecutar();
        });

        return { ejecutar: ejecutar, marcarTocado: function () { tocado = true; } };
    }

    // ── Toggle de mostrar/ocultar contraseña ──
    document.querySelectorAll('.btn-ojo').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = document.getElementById(btn.dataset.target);
            var icono = btn.querySelector('use');
            var esPassword = input.type === 'password';
            input.type = esPassword ? 'text' : 'password';
            icono.setAttribute('href', esPassword ? '#icon-eye-off' : '#icon-eye');
        });
    });

    // ── Tabs ──
    var tabLogin = document.getElementById('tab-login');
    var tabRegistro = document.getElementById('tab-registro');
    var formLogin = document.getElementById('form-login');
    var formRegistro = document.getElementById('form-registro');

    function activarTab(tab) {
        var esLogin = tab === 'login';
        tabLogin.classList.toggle('activo', esLogin);
        tabRegistro.classList.toggle('activo', !esLogin);
        formLogin.hidden = !esLogin;
        formRegistro.hidden = esLogin;
    }

    tabLogin.addEventListener('click', function () { activarTab('login'); });
    tabRegistro.addEventListener('click', function () { activarTab('registro'); });
    document.getElementById('btn-ir-login').addEventListener('click', function () { activarTab('login'); });

    // ── Validación en tiempo real (login) ──
    var loginCorreo = document.getElementById('login-correo');
    var loginPassword = document.getElementById('login-password');
    attachValidacionTiempoReal(loginCorreo, 'login-correo', validarCorreo);
    attachValidacionTiempoReal(loginPassword, 'login-password', validarPassword);

    // ── Validación en tiempo real (registro) ──
    var regNombre = document.getElementById('reg-nombre');
    var regCorreo = document.getElementById('reg-correo');
    var regPassword = document.getElementById('reg-password');
    var fuerzaBox = document.getElementById('fuerza-box');

    var tocadoRegNombre = false;
    function validarYLimpiarNombre() {
        tocadoRegNombre = true;
        var limpio = regNombre.value.replace(/[^A-Za-zÀ-ÿ\s]/g, '');
        if (limpio !== regNombre.value) {
            regNombre.value = limpio;
            setCampoEstado('reg-nombre', null, 'El nombre solo puede contener letras y espacios.');
            return;
        }
        setCampoEstado('reg-nombre', null, validarNombre(regNombre.value));
    }
    regNombre.addEventListener('focus', function () { tocadoRegNombre = true; });
    regNombre.addEventListener('input', validarYLimpiarNombre);
    regNombre.addEventListener('change', validarYLimpiarNombre);
    regNombre.addEventListener('blur', function () {
        tocadoRegNombre = true;
        setCampoEstado('reg-nombre', null, validarNombre(regNombre.value));
    });

    attachValidacionTiempoReal(regCorreo, 'reg-correo', validarCorreo);

    var reglasFuerza = [
        { key: 'longitud', test: function (v) { return v.length >= 6; } },
        { key: 'mayuscula', test: function (v) { return /[A-Z]/.test(v); } },
        { key: 'minuscula', test: function (v) { return /[a-z]/.test(v); } },
        { key: 'numero', test: function (v) { return /[0-9]/.test(v); } },
        { key: 'simbolo', test: function (v) { return /[^A-Za-z0-9]/.test(v); } },
    ];

    function actualizarFuerzaBox() {
        var valor = regPassword.value;
        fuerzaBox.hidden = valor.length === 0;
        reglasFuerza.forEach(function (regla) {
            var item = fuerzaBox.querySelector('[data-regla="' + regla.key + '"]');
            var ok = regla.test(valor);
            item.classList.toggle('ok', ok);
            item.querySelector('use').setAttribute('href', ok ? '#icon-check-circle' : '#icon-square');
        });
    }

    attachValidacionTiempoReal(regPassword, 'reg-password', validarPasswordFuerte, actualizarFuerzaBox);

    // ── Envío: LOGIN ──
    formLogin.addEventListener('submit', function (e) {
        e.preventDefault();

        var erroresCorreo = validarCorreo(loginCorreo.value);
        var erroresPassword = validarPassword(loginPassword.value);
        setCampoEstado('login-correo', null, erroresCorreo);
        setCampoEstado('login-password', null, erroresPassword);
        if (erroresCorreo || erroresPassword) return;

        var boton = document.getElementById('btn-login');
        alternarCarga(boton, true);
        var mensajeEl = document.getElementById('login-mensaje');
        mensajeEl.hidden = true;

        fetch('/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ correo: loginCorreo.value.trim(), password: loginPassword.value })
        })
            .then(function (res) { return res.json().then(function (body) { return { status: res.status, body: body }; }); })
            .then(function (r) {
                if (r.body.success) {
                    // La cookie HttpOnly con el JWT ya llegó sola en esta respuesta
                    // (Set-Cookie) — nada que guardar acá.
                    mostrarToast(r.body.mensaje || 'Bienvenido', 'exito');

                    if (r.body.data.usuario.rol === 'admin') {
                        mensajeEl.textContent = 'Sesión iniciada. Redirigiendo...';
                        mensajeEl.className = 'mensaje-general exito';
                        mensajeEl.hidden = false;
                        setTimeout(function () { window.location.href = '/'; }, 600);
                    } else {
                        mensajeEl.textContent =
                            'Sesión iniciada correctamente. El catálogo para clientes se habilitará en una etapa posterior.';
                        mensajeEl.className = 'mensaje-general exito';
                        mensajeEl.hidden = false;
                    }
                } else {
                    setCampoEstado('login-password', null, r.body.mensaje || 'Correo o contraseña incorrectos.');
                }
            })
            .catch(function () {
                mostrarToast('No se pudo conectar al servidor. Verifica tu red.', 'error');
            })
            .finally(function () { alternarCarga(boton, false); });
    });

    // ── Envío: REGISTRO ──
    formRegistro.addEventListener('submit', function (e) {
        e.preventDefault();

        var errNombre = validarNombre(regNombre.value);
        var errCorreo = validarCorreo(regCorreo.value);
        var errPassword = validarPasswordFuerte(regPassword.value);
        setCampoEstado('reg-nombre', null, errNombre);
        setCampoEstado('reg-correo', null, errCorreo);
        setCampoEstado('reg-password', null, errPassword);
        if (errNombre || errCorreo || errPassword) return;

        var boton = document.getElementById('btn-registro');
        alternarCarga(boton, true);
        var mensajeEl = document.getElementById('registro-mensaje');
        mensajeEl.hidden = true;

        fetch('/api/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                nombre: regNombre.value.trim(),
                correo: regCorreo.value.trim().toLowerCase(),
                password: regPassword.value
            })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (body.success) {
                    mostrarToast('Ya puedes iniciar sesión con tus datos.', 'exito');
                    loginCorreo.value = regCorreo.value;
                    regNombre.value = '';
                    regCorreo.value = '';
                    regPassword.value = '';
                    fuerzaBox.hidden = true;
                    activarTab('login');
                } else {
                    setCampoEstado('reg-correo', null, body.mensaje || 'No se pudo crear la cuenta.');
                }
            })
            .catch(function () {
                mostrarToast('No se pudo conectar al servidor. Verifica tu red.', 'error');
            })
            .finally(function () { alternarCarga(boton, false); });
    });

    function alternarCarga(boton, cargando) {
        boton.disabled = cargando;
        boton.querySelector('.btn-texto').hidden = cargando;
        boton.querySelector('.spinner').hidden = !cargando;
    }

        // ── Modal recuperar contraseña (3 llamadas: solicitar → validar → cambiar) ──
    var modal = document.getElementById('modal-recuperar');
    var recCorreo = document.getElementById('rec-correo');
    var recCodigo = document.getElementById('rec-codigo');
    var recPassword = document.getElementById('rec-password');
    var recPassword2 = document.getElementById('rec-password2');
    var recFuerzaBox = document.getElementById('rec-fuerza-box');
    var recError = document.getElementById('rec-error');
    var recPaso1 = document.getElementById('rec-paso-1');
    var recPaso2 = document.getElementById('rec-paso-2');
    var btnReenviar = document.getElementById('btn-reenviar-codigo');
    var temporizadorReenvio = null;

    function recMostrarError(msg) {
        recError.textContent = msg;
        recError.hidden = !msg;
    }

    function recIrPaso(n) {
        recPaso1.hidden = n !== 1;
        recPaso2.hidden = n !== 2;
        recMostrarError('');
        document.querySelectorAll('#rec-pasos .paso').forEach(function (p) {
            var num = parseInt(p.dataset.paso, 10);
            p.classList.toggle('activo', num === n);
            p.classList.toggle('completado', num < n);
        });
        setTimeout(function () { (n === 1 ? recCorreo : recCodigo).focus(); }, 50);
    }

    function recValidarCodigo(valor) {
        if (!valor || !valor.trim()) return 'Ingresa el código que te llegó al correo.';
        if (!/^\d{6}$/.test(valor.trim())) return 'El código debe tener 6 dígitos numéricos.';
        return '';
    }

    function recValidarConfirmacion(valor) {
        if (!valor) return 'Confirma tu nueva contraseña.';
        if (valor !== recPassword.value) return 'Las contraseñas no coinciden.';
        return '';
    }

    function recActualizarFuerza() {
        var valor = recPassword.value;
        recFuerzaBox.hidden = valor.length === 0;
        reglasFuerza.forEach(function (regla) {
            var item = recFuerzaBox.querySelector('[data-regla="' + regla.key + '"]');
            var ok = regla.test(valor);
            item.classList.toggle('ok', ok);
            item.querySelector('use').setAttribute('href', ok ? '#icon-check-circle' : '#icon-square');
        });
    }

    function recCerrar() {
        modal.hidden = true;
        if (temporizadorReenvio) { clearInterval(temporizadorReenvio); temporizadorReenvio = null; }
        recCodigo.value = '';
        recPassword.value = '';
        recPassword2.value = '';
        recFuerzaBox.hidden = true;
        recMostrarError('');
        recIrPaso(1);
    }

    function iniciarContadorReenvio() {
        var restante = 60;
        btnReenviar.disabled = true;
        btnReenviar.textContent = 'Reenviar código (' + restante + 's)';
        if (temporizadorReenvio) clearInterval(temporizadorReenvio);
        temporizadorReenvio = setInterval(function () {
            restante -= 1;
            if (restante <= 0) {
                clearInterval(temporizadorReenvio);
                temporizadorReenvio = null;
                btnReenviar.disabled = false;
                btnReenviar.textContent = 'Reenviar código';
                return;
            }
            btnReenviar.textContent = 'Reenviar código (' + restante + 's)';
        }, 1000);
    }

    // Validaciones en tiempo real dentro del modal
    attachValidacionTiempoReal(recCorreo, 'rec-correo', validarCorreo);
    attachValidacionTiempoReal(recCodigo, 'rec-codigo', recValidarCodigo);
    attachValidacionTiempoReal(recPassword, 'rec-password', validarPasswordFuerte, recActualizarFuerza);
    attachValidacionTiempoReal(recPassword2, 'rec-password2', recValidarConfirmacion);

    // El código solo acepta dígitos
    recCodigo.addEventListener('input', function () {
        recCodigo.value = recCodigo.value.replace(/\D/g, '').slice(0, 6);
    });

    // Abrir / cerrar
    document.getElementById('btn-abrir-recuperar').addEventListener('click', function () {
        recCorreo.value = loginCorreo.value;
        recCerrar();
        modal.hidden = false;
        setTimeout(function () { recCorreo.focus(); }, 50);
    });
    document.getElementById('btn-cerrar-recuperar').addEventListener('click', recCerrar);
    modal.addEventListener('click', function (e) { if (e.target === modal) recCerrar(); });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && !modal.hidden) recCerrar();
    });
    document.getElementById('btn-volver-paso1').addEventListener('click', function () { recIrPaso(1); });

    // ── PASO 1: solicitar el código ──
    function solicitarCodigo(boton) {
        var err = validarCorreo(recCorreo.value);
        setCampoEstado('rec-correo', null, err);
        if (err) return;

        alternarCarga(boton, true);
        recMostrarError('');

        fetch('/api/auth/recuperar/solicitar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ correo: recCorreo.value.trim().toLowerCase() })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (body.success) {
                    document.getElementById('rec-correo-eco').textContent = recCorreo.value.trim().toLowerCase();
                    mostrarToast(body.mensaje || 'Código enviado a tu correo.', 'exito');
                    recIrPaso(2);
                    iniciarContadorReenvio();
                } else {
                    recMostrarError(body.mensaje || 'No pudimos enviar el código. Intenta de nuevo.');
                }
            })
            .catch(function () {
                recMostrarError('No se pudo conectar al servidor. Verifica tu red.');
            })
            .finally(function () { alternarCarga(boton, false); });
    }

    document.getElementById('btn-enviar-codigo').addEventListener('click', function () {
        solicitarCodigo(this);
    });

    btnReenviar.addEventListener('click', function () {
        if (btnReenviar.disabled) return;
        recMostrarError('');
        fetch('/api/auth/recuperar/solicitar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ correo: recCorreo.value.trim().toLowerCase() })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (body.success) {
                    mostrarToast('Te enviamos un código nuevo.', 'exito');
                    recCodigo.value = '';
                    iniciarContadorReenvio();
                } else {
                    recMostrarError(body.mensaje || 'No pudimos reenviar el código.');
                }
            })
            .catch(function () {
                recMostrarError('No se pudo conectar al servidor. Verifica tu red.');
            });
    });

    // Validación previa del código contra el servidor al completar los 6 dígitos
    recCodigo.addEventListener('blur', function () {
        if (!/^\d{6}$/.test(recCodigo.value)) return;
        fetch('/api/auth/recuperar/validar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                correo: recCorreo.value.trim().toLowerCase(),
                codigo: recCodigo.value
            })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                setCampoEstado('rec-codigo', null,
                    body.success ? '' : (body.mensaje || 'El código no es válido o ya expiró.'));
            })
            .catch(function () { /* silencioso: el submit vuelve a validar */ });
    });

    // ── PASO 2: cambiar la contraseña ──
    document.getElementById('btn-cambiar-password').addEventListener('click', function () {
        var boton = this;
        var errCodigo = recValidarCodigo(recCodigo.value);
        var errPassword = validarPasswordFuerte(recPassword.value);
        var errConfirmar = recValidarConfirmacion(recPassword2.value);
        setCampoEstado('rec-codigo', null, errCodigo);
        setCampoEstado('rec-password', null, errPassword);
        setCampoEstado('rec-password2', null, errConfirmar);
        if (errCodigo || errPassword || errConfirmar) return;

        alternarCarga(boton, true);
        recMostrarError('');

        fetch('/api/auth/recuperar/cambiar', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                correo: recCorreo.value.trim().toLowerCase(),
                codigo: recCodigo.value.trim(),
                nuevaPassword: recPassword.value
            })
        })
            .then(function (res) { return res.json(); })
            .then(function (body) {
                if (body.success) {
                    var correoUsado = recCorreo.value.trim().toLowerCase();
                    recCerrar();
                    activarTab('login');
                    loginCorreo.value = correoUsado;
                    loginPassword.value = '';
                    loginPassword.focus();
                    mostrarToast(body.mensaje || 'Contraseña actualizada. Inicia sesión.', 'exito');
                } else {
                    recMostrarError(body.mensaje || 'No se pudo cambiar la contraseña.');
                }
            })
            .catch(function () {
                recMostrarError('No se pudo conectar al servidor. Verifica tu red.');
            })
            .finally(function () { alternarCarga(boton, false); });
    });

    // Enter dentro del modal avanza el paso actual
    modal.addEventListener('keydown', function (e) {
        if (e.key !== 'Enter') return;
        e.preventDefault();
        if (!recPaso1.hidden) document.getElementById('btn-enviar-codigo').click();
        else document.getElementById('btn-cambiar-password').click();
    });


    // ── Pestaña inicial (definida por el servidor vía Thymeleaf) ──
    document.addEventListener('DOMContentLoaded', function () {
        activarTab(window.OLYMPIC_TAB_INICIAL === 'registro' ? 'registro' : 'login');
    });
})();