package com.olympic.olympic.repository;

import com.olympic.olympic.entity.PasswordReset;
import com.olympic.olympic.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Integer> {

    Optional<PasswordReset> findByUsuarioAndToken(Usuario usuario, String token);

    void deleteByUsuario(Usuario usuario);
}