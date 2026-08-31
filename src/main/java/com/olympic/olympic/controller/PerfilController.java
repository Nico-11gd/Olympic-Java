package com.olympic.olympic.controller;

import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Controller
@RequestMapping("/perfil")
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    public PerfilController(UsuarioRepository usuarioRepository,
                            @Value("${app.upload.dir}") String uploadDir) {
        this.usuarioRepository = usuarioRepository;
        this.uploadDir = uploadDir;
    }

    @PostMapping("/foto")
    public String subirFoto(@RequestParam("archivoFoto") MultipartFile archivo,
                            RedirectAttributes redirectAttributes) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        String correo = auth.getName();
        Usuario usuario = usuarioRepository.findByCorreo(correo).orElse(null);
        if (usuario == null) {
            redirectAttributes.addFlashAttribute("perfilError", "Usuario no encontrado.");
            return "redirect:/admin";
        }

        if (archivo.isEmpty()) {
            redirectAttributes.addFlashAttribute("perfilError", "Selecciona una imagen.");
            return "redirect:/admin";
        }

        if (archivo.getSize() > MAX_SIZE) {
            redirectAttributes.addFlashAttribute("perfilError", "La imagen no puede superar 5 MB.");
            return "redirect:/admin";
        }

        String originalName = archivo.getOriginalFilename();
        String extension = "";
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            redirectAttributes.addFlashAttribute("perfilError", "Formato no válido. Usa JPG, PNG o WEBP.");
            return "redirect:/admin";
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

        return "redirect:/admin";
    }
}
