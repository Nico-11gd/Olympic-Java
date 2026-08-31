package com.olympic.olympic.config;

import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTest implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public DatabaseTest(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {

        System.out.println("========================================");
        System.out.println("     PRUEBA DE CONEXIÓN A DATABASE");
        System.out.println("========================================");

        usuarioRepository.findByCorreo("admin@olympic.com")
                .ifPresentOrElse(
                        usuario -> mostrarUsuario(usuario),
                        () -> System.out.println("❌ Usuario no encontrado")
                );

        System.out.println("========================================");
    }

    private void mostrarUsuario(Usuario usuario) {

        System.out.println("✅ Usuario encontrado");
        System.out.println("ID: " + usuario.getId());
        System.out.println("Nombre: " + usuario.getNombre());
        System.out.println("Correo: " + usuario.getCorreo());
        System.out.println("Rol: " + usuario.getRol());
        System.out.println("Activo: " + usuario.getActivo());
    }
}