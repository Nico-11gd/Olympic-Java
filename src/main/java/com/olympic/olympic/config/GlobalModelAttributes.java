package com.olympic.olympic.config;

import com.olympic.olympic.entity.Configuracion;
import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.repository.ConfiguracionRepository;
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
    private final ConfiguracionRepository configuracionRepository;

    public GlobalModelAttributes(UsuarioRepository usuarioRepository,
            ConfiguracionRepository configuracionRepository) {
        this.usuarioRepository = usuarioRepository;
        this.configuracionRepository = configuracionRepository;
    }

    @ModelAttribute("usuarioActual")
    public Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            return usuarioRepository.findByCorreo(auth.getName()).orElse(null);
        }
        return null;
    }

    @ModelAttribute("nombreTienda")
    public String nombreTienda() {
        return configuracionRepository.findByClave("nombre_tienda")
                .map(Configuracion::getValor)
                .filter(valor -> valor != null && !valor.isBlank())
                .orElse("Olympic Store");
    }
}