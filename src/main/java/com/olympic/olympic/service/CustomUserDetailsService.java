package com.olympic.olympic.service;

import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Reemplaza al login basado en JWT: Spring Security usa esta clase para
 * buscar al usuario por correo y comparar la contraseña (BCrypt) en cada
 * intento de POST /login. La sesión queda guardada del lado del servidor
 * (JSESSIONID por cookie), no hay ningún token ni JavaScript involucrado.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("No existe un usuario con el correo: " + correo));

        return new User(
                usuario.getCorreo(),
                usuario.getPassword(),
                Boolean.TRUE.equals(usuario.getActivo()),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }
}
