package com.olympic.olympic.service.impl;

import com.olympic.olympic.dto.LoginRequest;
import com.olympic.olympic.dto.LoginResponse;
import com.olympic.olympic.dto.RegistroRequest;
import com.olympic.olympic.dto.UsuarioResponse;
import com.olympic.olympic.entity.Rol;
import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.exception.CredencialesInvalidasException;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.exception.UsuarioInactivoException;
import com.olympic.olympic.repository.UsuarioRepository;
import com.olympic.olympic.security.JwtService;
import com.olympic.olympic.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Lógica de autenticación equivalente a model/Usuario.php (login / registrar),
 * pero con BCrypt real vía Spring Security y emisión de JWT.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo().trim().toLowerCase())
                .orElseThrow(() -> new CredencialesInvalidasException("Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new CredencialesInvalidasException("Correo o contraseña incorrectos");
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new UsuarioInactivoException("Tu cuenta está inactiva. Contacta al administrador.");
        }

        String token = jwtService.generarToken(usuario);
        return new LoginResponse(token, UsuarioResponse.fromEntity(usuario));
    }

    @Override
    @Transactional
    public void registrar(RegistroRequest request) {
        String correo = request.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreo(correo)) {
            throw new RecursoDuplicadoException("Ese correo ya está registrado");
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
