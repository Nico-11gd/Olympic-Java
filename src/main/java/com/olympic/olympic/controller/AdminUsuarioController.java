package com.olympic.olympic.controller;

import com.olympic.olympic.dto.UsuarioCreacionRequest;
import com.olympic.olympic.dto.UsuarioRequest;
import com.olympic.olympic.dto.UsuarioResponse;
import com.olympic.olympic.entity.Rol;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
public class AdminUsuarioController {

    private final UsuarioService usuarioService;

    public AdminUsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(Rol.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(Rol.fromValor(text));
            }
        });
    }

    @GetMapping
    public String listar(@RequestParam(name = "estado", required = false, defaultValue = "todos") String estado,
                          @RequestParam(name = "q", required = false) String q,
                          Model model) {

        List<UsuarioResponse> usuarios = usuarioService.obtenerTodos();

        if (q != null && !q.isBlank()) {
            String buscado = q.trim().toLowerCase();
            usuarios = usuarios.stream()
                    .filter(u -> u.getNombre().toLowerCase().contains(buscado)
                            || u.getCorreo().toLowerCase().contains(buscado))
                    .toList();
        }

        usuarios = switch (estado) {
            case "activos" -> usuarios.stream().filter(UsuarioResponse::getActivo).toList();
            case "inactivos" -> usuarios.stream().filter(u -> !u.getActivo()).toList();
            case "admin" -> usuarios.stream().filter(u -> u.getRol() == Rol.ADMIN).toList();
            case "clientes" -> usuarios.stream().filter(u -> u.getRol() == Rol.CLIENTE).toList();
            default -> usuarios;
        };

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", q);

        if (!model.containsAttribute("usuarioForm")) {
            model.addAttribute("usuarioForm", new UsuarioCreacionRequest());
            model.addAttribute("usuarioFormId", null);
        }

        Object idFlash = model.asMap().get("usuarioFormId");
        UsuarioCreacionRequest formularioNuevo = (idFlash == null)
                ? (UsuarioCreacionRequest) model.asMap().get("usuarioForm")
                : new UsuarioCreacionRequest();
        model.addAttribute("usuarioFormNuevo", formularioNuevo);

        return "admin/usuarios";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("usuarioForm") UsuarioCreacionRequest form,
                         BindingResult resultado,
                         RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return reabrirConError(form, null, primerError(resultado), redirectAttributes, "#modal-nuevo");
        }

        try {
            usuarioService.crear(form);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario creado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (RecursoDuplicadoException e) {
            return reabrirConError(form, null, e.getMessage(), redirectAttributes, "#modal-nuevo");
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Integer id,
                              @ModelAttribute("usuarioForm") UsuarioRequest form,
                              BindingResult resultado,
                              RedirectAttributes redirectAttributes) {

        try {
            usuarioService.actualizar(id, form);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario actualizado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (RecursoDuplicadoException | RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute("usuarioFormError", e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
                                 @RequestParam("activo") boolean activo,
                                 RedirectAttributes redirectAttributes) {
        usuarioService.cambiarEstado(id, activo);
        redirectAttributes.addFlashAttribute("mensaje", activo ? "Usuario activado." : "Usuario desactivado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id,
                            RedirectAttributes redirectAttributes) {
        try {
            usuarioService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Usuario eliminado correctamente.");
        } catch (RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    private String reabrirConError(Object form, Integer id, String error,
                                    RedirectAttributes redirectAttributes, String ancla) {
        redirectAttributes.addFlashAttribute("usuarioForm", form);
        redirectAttributes.addFlashAttribute("usuarioFormId", id);
        redirectAttributes.addFlashAttribute("usuarioFormError", error);
        return "redirect:/admin/usuarios" + ancla;
    }

    private String primerError(BindingResult resultado) {
        FieldError error = resultado.getFieldError();
        return error != null ? error.getDefaultMessage() : "Revisa los datos del formulario.";
    }
}
