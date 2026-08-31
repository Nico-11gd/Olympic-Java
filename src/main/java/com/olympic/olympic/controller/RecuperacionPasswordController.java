package com.olympic.olympic.controller;

import com.olympic.olympic.exception.CodigoInvalidoException;
import com.olympic.olympic.service.PasswordResetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Flujo de "olvidé mi contraseña" en 2 páginas (antes era un modal con JS):
 * 1) /recuperar-password        -> pide el correo, envía el código.
 * 2) /recuperar-password/codigo  -> pide código + contraseña nueva.
 */
@Controller
@org.springframework.web.bind.annotation.RequestMapping("/recuperar-password")
public class RecuperacionPasswordController {

    private final PasswordResetService passwordResetService;

    public RecuperacionPasswordController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping
    public String mostrarSolicitud() {
        return "recuperar-password";
    }

    @PostMapping
    public String solicitar(@RequestParam("correo") String correo, RedirectAttributes redirectAttributes) {
        passwordResetService.solicitarCodigo(correo);
        redirectAttributes.addFlashAttribute("mensaje",
                "Si el correo está registrado, te enviamos un código de recuperación.");
        redirectAttributes.addAttribute("correo", correo);
        return "redirect:/recuperar-password/codigo";
    }

    @GetMapping("/codigo")
    public String mostrarCodigo(@RequestParam("correo") String correo, Model model) {
        model.addAttribute("correo", correo);
        return "recuperar-password-codigo";
    }

    @PostMapping("/codigo")
    public String cambiar(@RequestParam("correo") String correo,
                           @RequestParam("codigo") String codigo,
                           @RequestParam("nuevaPassword") String nuevaPassword,
                           @RequestParam("confirmarPassword") String confirmarPassword,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (!nuevaPassword.equals(confirmarPassword)) {
            model.addAttribute("correo", correo);
            model.addAttribute("error", "Las contraseñas no coinciden.");
            return "recuperar-password-codigo";
        }

        try {
            passwordResetService.validarCodigo(correo, codigo);
            passwordResetService.cambiarPassword(correo, codigo, nuevaPassword);
        } catch (CodigoInvalidoException e) {
            model.addAttribute("correo", correo);
            model.addAttribute("error", e.getMessage());
            return "recuperar-password-codigo";
        }

        redirectAttributes.addFlashAttribute("mensaje", "Contraseña actualizada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
