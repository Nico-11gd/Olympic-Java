package com.olympic.olympic.controller;

import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

/**
 * Acciones de cuenta del usuario autenticado (cliente o admin):
 * subir foto de perfil y actualizar nombre/contraseña. Al terminar vuelve
 * a la página donde estaba (Referer interno) para que la acción funcione en
 * cualquier panel.
 */
@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;
    private static final int MIN_PASSWORD = 6;

    public PerfilController(UsuarioRepository usuarioRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.upload.dir}") String uploadDir) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.uploadDir = uploadDir;
    }

    @PostMapping("/foto")
    public String subirFoto(@RequestParam("archivoFoto") MultipartFile archivo,
                            HttpServletRequest request,
                            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioActual();
        if (usuario == null) {
            return "redirect:/login";
        }

        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("perfilError", "Selecciona una imagen.");
            return destino(request);
        }

        if (archivo.getSize() > MAX_SIZE) {
            redirectAttributes.addFlashAttribute("perfilError", "La imagen no puede superar 5 MB.");
            return destino(request);
        }

        String originalName = archivo.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            redirectAttributes.addFlashAttribute("perfilError", "Formato no válido. Usa JPG, PNG o WEBP.");
            return destino(request);
        }

        try {
            Path perfilDir = Paths.get(uploadDir).resolve("perfiles");
            Files.createDirectories(perfilDir);

            String filename = "perfil_" + UUID.randomUUID() + "." + extension;
            archivo.transferTo(perfilDir.resolve(filename).toFile());

            usuario.setFotoPerfil(filename);
            usuarioRepository.save(usuario);

            redirectAttributes.addFlashAttribute("perfilMensaje", "Foto de perfil actualizada correctamente.");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("perfilError", "Error al guardar la imagen.");
        }

        return destino(request);
    }

    @PostMapping("/actualizar")
    public String actualizar(@RequestParam(name = "nombre") String nombre,
                             @RequestParam(name = "passwordActual", required = false) String passwordActual,
                             @RequestParam(name = "nuevaPassword", required = false) String nuevaPassword,
                             @RequestParam(name = "confirmarPassword", required = false) String confirmarPassword,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioActual();
        if (usuario == null) {
            return "redirect:/login";
        }

        if (nombre == null || nombre.isBlank()) {
            redirectAttributes.addFlashAttribute("perfilError", "El nombre no puede estar vacío.");
            return destino(request);
        }

        usuario.setNombre(nombre.trim());
        String mensaje = "Perfil actualizado correctamente.";

        boolean cambiaClave = nuevaPassword != null && !nuevaPassword.isBlank();
        if (cambiaClave) {
            if (passwordActual == null || !passwordEncoder.matches(passwordActual, usuario.getPassword())) {
                redirectAttributes.addFlashAttribute("perfilError", "La contraseña actual no es correcta.");
                return destino(request);
            }
            if (nuevaPassword.length() < MIN_PASSWORD) {
                redirectAttributes.addFlashAttribute("perfilError", "La nueva contraseña debe tener al menos 6 caracteres.");
                return destino(request);
            }
            if (!nuevaPassword.equals(confirmarPassword)) {
                redirectAttributes.addFlashAttribute("perfilError", "La confirmación de contraseña no coincide.");
                return destino(request);
            }
            usuario.setPassword(passwordEncoder.encode(nuevaPassword));
            mensaje = "Perfil y contraseña actualizados correctamente.";
        }

        usuarioRepository.save(usuario);
        redirectAttributes.addFlashAttribute("perfilMensaje", mensaje);
        return destino(request);
    }

    private Usuario usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }
        return usuarioRepository.findByCorreo(auth.getName()).orElse(null);
    }

    private String destino(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null && referer.startsWith("/") && !referer.startsWith("//")) {
            String contexto = request.getContextPath();
            if (referer.startsWith(contexto + "/") || referer.equals(contexto)) {
                return "redirect:" + referer;
            }
        }
        return "redirect:/admin";
    }
}