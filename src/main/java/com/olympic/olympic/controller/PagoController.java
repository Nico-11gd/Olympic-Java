package com.olympic.olympic.controller;

import com.olympic.olympic.dto.PagoRequest;
import com.olympic.olympic.dto.PagoResponse;
import com.olympic.olympic.entity.EstadoPago;
import com.olympic.olympic.entity.MetodoPago;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.service.PagoService;
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

/**
 * Módulo de pagos del panel admin. Mismo patrón que productos/usuarios:
 * modales de creación/edición con CSS puro (:target), filtros de estado,
 * búsqueda y redirección a la MISMA ancla cuando hay un error de formulario.
 */
@Controller
@RequestMapping("/admin/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(EstadoPago.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(EstadoPago.fromValor(text));
            }
        });
        binder.registerCustomEditor(MetodoPago.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(MetodoPago.fromValor(text));
            }
        });
        // EstadoPago se usa en el selector del formulario y en el toggle de la fila.
    }

    @GetMapping
    public String listar(@RequestParam(name = "estado", required = false, defaultValue = "todos") String estado,
                          @RequestParam(name = "q", required = false) String q,
                          Model model) {

        List<PagoResponse> pagos = pagoService.listar();

        if (q != null && !q.isBlank()) {
            String buscado = q.trim().toLowerCase();
            pagos = pagos.stream()
                    .filter(p -> String.valueOf(p.getId()).equals(buscado)
                            || (p.getPedidoId() != null && String.valueOf(p.getPedidoId()).equals(buscado))
                            || (p.getMetodo() != null && p.getMetodo().name().toLowerCase().contains(buscado))
                            || (p.getNombre() != null && p.getNombre().toLowerCase().contains(buscado))
                            || (p.getClienteCorreo() != null && p.getClienteCorreo().toLowerCase().contains(buscado)))
                    .toList();
        }

        pagos = switch (estado) {
            case "pendientes" -> pagos.stream()
                    .filter(p -> p.getEstado() == EstadoPago.PENDIENTE).toList();
            case "aprobados" -> pagos.stream()
                    .filter(p -> p.getEstado() == EstadoPago.APROBADO).toList();
            case "rechazados" -> pagos.stream()
                    .filter(p -> p.getEstado() == EstadoPago.RECHAZADO).toList();
            default -> pagos;
        };

        model.addAttribute("pagos", pagos);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", q);
        model.addAttribute("pedidos", pagoService.pedidosDisponibles());
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("estadosPago", EstadoPago.values());

        if (!model.containsAttribute("pagoForm")) {
            model.addAttribute("pagoForm", new PagoRequest());
            model.addAttribute("pagoFormId", null);
        }

        Object idFlash = model.asMap().get("pagoFormId");
        PagoRequest formularioNuevo = (idFlash == null)
                ? (PagoRequest) model.asMap().get("pagoForm")
                : new PagoRequest();
        model.addAttribute("pagoFormNuevo", formularioNuevo);

        return "admin/pagos";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("pagoForm") PagoRequest form,
                         BindingResult resultado,
                         RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return reabrirConError(form, null, primerError(resultado), redirectAttributes, "#modal-nuevo");
        }

        try {
            pagoService.crear(form);
            redirectAttributes.addFlashAttribute("mensaje", "Pago creado correctamente.");
            return "redirect:/admin/pagos";
        } catch (RecursoNoEncontradoException e) {
            return reabrirConError(form, null, e.getMessage(), redirectAttributes, "#modal-nuevo");
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Integer id,
                              @Valid @ModelAttribute("pagoForm") PagoRequest form,
                              BindingResult resultado,
                              RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return reabrirConError(form, id, primerError(resultado), redirectAttributes, "#modal-editar-" + id);
        }

        try {
            pagoService.actualizar(id, form);
            redirectAttributes.addFlashAttribute("mensaje", "Pago actualizado correctamente.");
            return "redirect:/admin/pagos";
        } catch (RecursoNoEncontradoException e) {
            return reabrirConError(form, id, e.getMessage(), redirectAttributes, "#modal-editar-" + id);
        }
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
                                 @RequestParam("estado") String estado,
                                 RedirectAttributes redirectAttributes) {

        try {
            EstadoPago nuevoEstado = EstadoPago.fromValor(estado);
            pagoService.cambiarEstado(id, nuevoEstado);
            redirectAttributes.addFlashAttribute("mensaje", "Pago " + nuevoEstado.getValor() + ".");
        } catch (RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
        }
        return "redirect:/admin/pagos";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Integer id,
                            RedirectAttributes redirectAttributes) {
        try {
            pagoService.eliminar(id);
            redirectAttributes.addFlashAttribute("mensaje", "Pago eliminado correctamente.");
        } catch (RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute("mensaje", e.getMessage());
        }
        return "redirect:/admin/pagos";
    }

    private String reabrirConError(Object form, Integer id, String error,
                                    RedirectAttributes redirectAttributes, String ancla) {
        redirectAttributes.addFlashAttribute("pagoForm", form);
        redirectAttributes.addFlashAttribute("pagoFormId", id);
        redirectAttributes.addFlashAttribute("pagoFormError", error);
        return "redirect:/admin/pagos" + ancla;
    }

    private String primerError(BindingResult resultado) {
        FieldError error = resultado.getFieldError();
        return error != null ? error.getDefaultMessage() : "Revisa los datos del formulario.";
    }
}