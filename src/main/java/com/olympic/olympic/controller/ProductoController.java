package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ImagenSubidaResponse;
import com.olympic.olympic.dto.ProductoRequest;
import com.olympic.olympic.dto.ProductoResponse;
import com.olympic.olympic.entity.Categoria;
import com.olympic.olympic.entity.TipoTalla;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.repository.CategoriaRepository;
import com.olympic.olympic.repository.PromocionRepository;
import com.olympic.olympic.service.ImagenProductoService;
import com.olympic.olympic.service.ProductoService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

/**
 * Módulo de productos del panel admin. Crear/editar ya NO son páginas
 * aparte: son modales flotantes dentro de /admin/productos, abiertos con
 * anclas de URL (#modal-nuevo, #modal-editar-{id}) y CSS puro (:target) —
 * sin una sola línea de JavaScript. Si el formulario tiene un error, se
 * redirige de vuelta a la MISMA ancla para que el modal se vea abierto otra
 * vez, con el mensaje de error (vía flash attributes).
 */
@Controller
@RequestMapping("/admin/productos")
public class ProductoController {

    private final ProductoService productoService;
    private final ImagenProductoService imagenProductoService;
    private final CategoriaRepository categoriaRepository;
    private final PromocionRepository promocionRepository;

    public ProductoController(ProductoService productoService,
                               ImagenProductoService imagenProductoService,
                               CategoriaRepository categoriaRepository,
                               PromocionRepository promocionRepository) {
        this.productoService = productoService;
        this.imagenProductoService = imagenProductoService;
        this.categoriaRepository = categoriaRepository;
        this.promocionRepository = promocionRepository;
    }

    @GetMapping
    public String listar(@RequestParam(name = "estado", required = false, defaultValue = "todos") String estado,
                          @RequestParam(name = "q", required = false) String q,
                          @RequestParam(name = "categoriaId", required = false) Integer categoriaId,
                          Model model) {

        List<ProductoResponse> productos = productoService.listar(true);

        if (q != null && !q.isBlank()) {
            String buscado = q.trim().toLowerCase();
            productos = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(buscado)
                            || (p.getCategoria() != null && p.getCategoria().toLowerCase().contains(buscado)))
                    .toList();
        }

        productos = switch (estado) {
            case "activos" -> productos.stream().filter(ProductoResponse::getActivo).toList();
            case "inactivos" -> productos.stream().filter(p -> !p.getActivo()).toList();
            case "bajo" -> productos.stream().filter(p -> p.getStock() <= 5).toList();
            default -> productos;
        };

        TipoTalla tipoTalla = TipoTalla.NINGUNA;
        if (categoriaId != null) {
            Categoria cat = categoriaRepository.findById(categoriaId).orElse(null);
            if (cat != null) {
                tipoTalla = cat.getTipoTalla();
            }
        }

        model.addAttribute("productos", productos);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", q);
        model.addAttribute("categorias", categoriaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("promociones", promocionRepository.findAll());
        model.addAttribute("tipoTallaSeleccionada", tipoTalla);
        model.addAttribute("categoriaIdSeleccionada", categoriaId);

        if (!model.containsAttribute("productoForm")) {
            model.addAttribute("productoForm", new ProductoRequest());
            model.addAttribute("productoFormId", null);
        }

        // El modal "Nuevo" solo debe repoblarse con datos del flash si el error
        // que acaba de ocurrir fue justamente al CREAR (productoFormId == null).
        // Si el error fue al EDITAR un producto existente, el modal "Nuevo" debe
        // quedar vacío — por eso se resuelve acá y no con "new" dentro de Thymeleaf.
        Object idFlash = model.asMap().get("productoFormId");
        ProductoRequest formularioNuevo = (idFlash == null)
                ? (ProductoRequest) model.asMap().get("productoForm")
                : new ProductoRequest();
        model.addAttribute("productoFormNuevo", formularioNuevo);

        return "admin/productos";
    }

    @PostMapping("/nuevo")
    public String crear(@Valid @ModelAttribute("productoForm") ProductoRequest producto,
                         BindingResult resultado,
                         @RequestParam(name = "archivoImagen", required = false) MultipartFile[] archivosImagen,
                         RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return reabrirConError(producto, null, primerError(resultado), redirectAttributes, "#modal-nuevo");
        }

        try {
            String imagenes = subirImagenes(archivosImagen);
            if (imagenes != null) {
                producto.setImagen(imagenes);
            }
            productoService.crear(producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto creado correctamente.");
            return "redirect:/admin/productos";
        } catch (IllegalArgumentException | RecursoDuplicadoException | RecursoNoEncontradoException e) {
            return reabrirConError(producto, null, e.getMessage(), redirectAttributes, "#modal-nuevo");
        }
    }

    @PostMapping("/{id}/editar")
    public String actualizar(@PathVariable Integer id,
                              @Valid @ModelAttribute("productoForm") ProductoRequest producto,
                              BindingResult resultado,
                              @RequestParam(name = "archivoImagen", required = false) MultipartFile[] archivosImagen,
                              RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            return reabrirConError(producto, id, primerError(resultado), redirectAttributes, "#modal-editar-" + id);
        }

        try {
            String imagenes = subirImagenes(archivosImagen);
            if (imagenes != null) {
                producto.setImagen(imagenes);
            }
            productoService.actualizar(id, producto);
            redirectAttributes.addFlashAttribute("mensaje", "Producto actualizado correctamente.");
            return "redirect:/admin/productos";
        } catch (IllegalArgumentException | RecursoDuplicadoException | RecursoNoEncontradoException e) {
            return reabrirConError(producto, id, e.getMessage(), redirectAttributes, "#modal-editar-" + id);
        }
    }

    /**
     * Sube todas las fotos seleccionadas y devuelve sus nombres unidos por
     * coma (portada primero). Devuelve null si no se seleccionó ninguna.
     */
    private String subirImagenes(MultipartFile[] archivos) {
        if (archivos == null || archivos.length == 0) {
            return null;
        }
        List<String> nombres = new ArrayList<>();
        for (MultipartFile archivo : archivos) {
            if (archivo == null || archivo.isEmpty()) {
                continue;
            }
            ImagenSubidaResponse subida = imagenProductoService.subir(archivo);
            nombres.add(subida.getFilename());
        }
        return nombres.isEmpty() ? null : String.join(",", nombres);
    }

    @PostMapping("/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
                                 @RequestParam("activo") boolean activo,
                                 RedirectAttributes redirectAttributes) {
        productoService.cambiarEstado(id, activo);
        redirectAttributes.addFlashAttribute("mensaje", activo ? "Producto activado." : "Producto desactivado.");
        return "redirect:/admin/productos";
    }

    private String reabrirConError(ProductoRequest producto, Integer id, String error,
                                    RedirectAttributes redirectAttributes, String ancla) {
        redirectAttributes.addFlashAttribute("productoForm", producto);
        redirectAttributes.addFlashAttribute("productoFormId", id);
        redirectAttributes.addFlashAttribute("productoFormError", error);
        return "redirect:/admin/productos" + ancla;
    }

    private String primerError(BindingResult resultado) {
        FieldError error = resultado.getFieldError();
        return error != null ? error.getDefaultMessage() : "Revisa los datos del formulario.";
    }
}
