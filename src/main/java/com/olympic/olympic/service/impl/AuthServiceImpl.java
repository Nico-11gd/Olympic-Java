package com.olympic.olympic.service.impl;

import com.olympic.olympic.dto.RegistroRequest;
import com.olympic.olympic.entity.Rol;
import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.repository.UsuarioRepository;
import com.olympic.olympic.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Lógica de registro equivalente a model/Usuario.php::registrar(), con
 * BCrypt real vía Spring Security. El login ya no se maneja aquí (ver
 * CustomUserDetailsService + SecurityConfig).
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registrar(RegistroRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RecursoDuplicadoException("Ese correo ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre().trim());
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        // Nunca se permite elegir el rol desde el formulario público: siempre CLIENTE.
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        usuario.setCreatedAt(LocalDateTime.now());

        usuarioRepository.save(usuario);
    }
}
