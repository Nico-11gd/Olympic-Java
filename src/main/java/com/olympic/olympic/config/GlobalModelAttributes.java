package com.olympic.olympic.config;

import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.repository.UsuarioRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Expone el usuario autenticado a TODOS los templates (tienda y panel admin),
 * para que la foto de perfil y el nombre se muestren y se actualicen siempre,
 * en cualquier página y panel.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final UsuarioRepository usuarioRepository;

    public GlobalModelAttributes(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return usuarioRepository.findByCorreo(auth.getName()).orElse(null);
        }
        return null;
    }
}