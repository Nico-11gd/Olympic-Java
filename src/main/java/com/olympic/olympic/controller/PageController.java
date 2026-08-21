package com.olympic.olympic.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controlador de vistas (Thymeleaf).
 */
@Controller
public class PageController {

    // Página pública de la tienda (equivalente a app/(tabs)/catalogo.tsx):
    // cualquiera puede verla, con o sin sesión — el login solo se exige al comprar.
    @GetMapping("/")
    public String raiz() {
        return "inicio";
    }

    @GetMapping("/login")
    public String login(@RequestParam(name = "tab", required = false) String tab, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            String home = homePorRol(auth);
            if (home != null) {
                return "redirect:" + home;
            }
        }
        model.addAttribute("tabInicial", "registro".equalsIgnoreCase(tab) ? "registro" : "login");
        return "login";
    }

    /**
     * Panel correspondiente al rol autenticado.
     */
    private String homePorRol(Authentication auth) {
        // Tanto ADMIN como CLIENTE caen ahora en la home pública;
        // desde ahí el botón "Panel Admin" lleva al panel correspondiente.
        return "/";
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        model.addAttribute("tabInicial", "registro");
        return "login";
    }

    // Equivalente a app/admin/productos.tsx. Spring Security ya exige ADMIN
    // antes de llegar aquí — ver SecurityConfig.
    @GetMapping("/admin/productos")
    public String productos() {
        return "productos";
    }

    // Equivalente a app/producto/[id].tsx. Público — el login solo se exige
    // al hacer clic en "Añadir al carrito" (ver producto.js).
    @GetMapping("/producto")
    public String producto() {
        return "producto";
    }
}