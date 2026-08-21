package com.olympic.olympic.service.impl;

import com.olympic.olympic.entity.PasswordReset;
import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.exception.CodigoInvalidoException;
import com.olympic.olympic.repository.PasswordResetRepository;
import com.olympic.olympic.repository.UsuarioRepository;
import com.olympic.olympic.service.EmailService;
import com.olympic.olympic.service.PasswordResetService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.recuperacion-password.expiracion-minutos}")
    private int expiracionMinutos;

    public PasswordResetServiceImpl(UsuarioRepository usuarioRepository,
            PasswordResetRepository passwordResetRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void solicitarCodigo(String correo) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByCorreo(correo.trim().toLowerCase());
        if (usuarioOpt.isEmpty()) {
            // No revelamos si el correo existe o no en el sistema.
            return;
        }
        Usuario usuario = usuarioOpt.get();

        // Invalida cualquier código anterior pendiente para este usuario.
        passwordResetRepository.deleteByUsuario(usuario);

        String codigo = generarCodigo();

        PasswordReset reset = new PasswordReset();
        reset.setUsuario(usuario);
        reset.setToken(codigo);
        reset.setExpiraAt(LocalDateTime.now().plusMinutes(expiracionMinutos));
        reset.setCreatedAt(LocalDateTime.now());
        passwordResetRepository.save(reset);

        emailService.enviarCodigoRecuperacion(usuario.getCorreo(), usuario.getNombre(), codigo, expiracionMinutos);
    }

    @Override
    @Transactional(readOnly = true)
    public void validarCodigo(String correo, String codigo) {
        buscarTokenValido(correo, codigo);
    }

    @Override
    @Transactional
    public void cambiarPassword(String correo, String codigo, String nuevaPassword) {
        PasswordReset reset = buscarTokenValido(correo, codigo);

        Usuario usuario = reset.getUsuario();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuarioRepository.save(usuario);

        passwordResetRepository.deleteByUsuario(usuario);
    }

    private PasswordReset buscarTokenValido(String correo, String codigo) {
        Usuario usuario = usuarioRepository.findByCorreo(correo.trim().toLowerCase())
                .orElseThrow(() -> new CodigoInvalidoException("Código inválido o expirado."));

        PasswordReset reset = passwordResetRepository.findByUsuarioAndToken(usuario, codigo)
                .orElseThrow(() -> new CodigoInvalidoException("Código inválido o expirado."));

        if (reset.getExpiraAt().isBefore(LocalDateTime.now())) {
            passwordResetRepository.delete(reset);
            throw new CodigoInvalidoException("Código inválido o expirado.");
        }

        return reset;
    }

    private String generarCodigo() {
        int numero = RANDOM.nextInt(1_000_000);
        return String.format("%06d", numero);
    }
}