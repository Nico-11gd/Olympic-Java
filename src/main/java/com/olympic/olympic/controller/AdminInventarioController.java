package com.olympic.olympic.controller;

import com.olympic.olympic.entity.InventarioMovimiento;
import com.olympic.olympic.entity.Producto;
import com.olympic.olympic.entity.TipoMovimiento;
import com.olympic.olympic.repository.InventarioMovimientoRepository;
import com.olympic.olympic.repository.ProductoRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Módulo de inventario del panel admin: gestiona ENTRADAS y SALIDAS de stock
 * (compras a proveedor, devoluciones, daños, pérdidas y ajustes) siguiendo la
 * misma lógica del checkout: cada movimiento guarda stock_anterior/actual y
 * actualiza el stock del producto. Las ventas del checkout se registran solas.
 */
@Controller
@RequestMapping("/admin/inventario")
public class AdminInventarioController {

    private final ProductoRepository productoRepository;
    private final InventarioMovimientoRepository movimientoRepository;

    public AdminInventarioController(ProductoRepository productoRepository,
                                     InventarioMovimientoRepository movimientoRepository) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @GetMapping
    public String listar(@RequestParam(name = "tipo", required = false, defaultValue = "todos") String tipo,
                         @RequestParam(name = "q", required = false) String q,
                         Model model) {

        List<InventarioMovimiento> movimientos = movimientoRepository.findAllByOrderByIdDesc();

        if (q != null && !q.isBlank()) {
            String buscado = q.trim().toLowerCase();
            movimientos = movimientos.stream()
                    .filter(m -> (m.getProductoNombre() != null && m.getProductoNombre().toLowerCase().contains(buscado))
                            || (m.getMotivo() != null && m.getMotivo().toLowerCase().contains(buscado))
                            || (m.getUsuario() != null && m.getUsuario().toLowerCase().contains(buscado)))
                    .toList();
        }

        if (!"todos".equals(tipo)) {
            try {
                TipoMovimiento tipoFiltro = TipoMovimiento.fromValor(tipo);
                movimientos = movimientos.stream()
                        .filter(m -> m.getTipo() == tipoFiltro)
                        .toList();
            } catch (IllegalArgumentException ignored) {
                // filtro inválido -> mostrar todos
            }
        }

        model.addAttribute("movimientos", movimientos);
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("busqueda", q);
        model.addAttribute("productos", productoRepository.findAllByOrderByIdDesc());
        model.addAttribute("tiposMovimiento", List.of(
                TipoMovimiento.ENTRADA,
                TipoMovimiento.DEVOLUCION,
                TipoMovimiento.DANO,
                TipoMovimiento.PERDIDA,
                TipoMovimiento.AJUSTE));
        return "admin/inventario";
    }

    @PostMapping("/registrar")
    public String registrar(@RequestParam Integer productoId,
                            @RequestParam String tipo,
                            @RequestParam Integer cantidad,
                            @RequestParam(name = "motivo", required = false) String motivo,
                            RedirectAttributes redirectAttributes) {

        Producto producto = productoRepository.findById(productoId).orElse(null);
        if (producto == null) {
            redirectAttributes.addFlashAttribute("mensajeError", "Producto no encontrado.");
            return "redirect:/admin/inventario#modal-nuevo";
        }

        TipoMovimiento tipoMov;
        try {
            tipoMov = TipoMovimiento.fromValor(tipo);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("mensajeError", "Tipo de movimiento no válido.");
            return "redirect:/admin/inventario#modal-nuevo";
        }

        if (cantidad == null || cantidad == 0) {
            redirectAttributes.addFlashAttribute("mensajeError", "La cantidad debe ser diferente de 0.");
            return "redirect:/admin/inventario#modal-nuevo";
        }

        boolean permiteNegativos = tipoMov == TipoMovimiento.AJUSTE;
        if (!permiteNegativos && cantidad < 0) {
            redirectAttributes.addFlashAttribute("mensajeError", "La cantidad debe ser mayor a 0.");
            return "redirect:/admin/inventario#modal-nuevo";
        }

        boolean incrementa = switch (tipoMov) {
            case ENTRADA, DEVOLUCION -> true;
            case VENTA, DANO, PERDIDA -> false;
            case AJUSTE -> cantidad > 0;
        };
        int delta = incrementa ? cantidad : -Math.abs(cantidad);

        int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
        int stockNuevo = stockAnterior + delta;
        if (stockNuevo < 0) {
            redirectAttributes.addFlashAttribute("mensajeError",
                    "No hay suficiente stock de \"" + producto.getNombre() + "\" para esa salida. Actual: " + stockAnterior + ".");
            return "redirect:/admin/inventario#modal-nuevo";
        }

        producto.setStock(stockNuevo);
        productoRepository.save(producto);

        InventarioMovimiento movimiento = new InventarioMovimiento();
        movimiento.setProductoId(producto.getId());
        movimiento.setProductoNombre(producto.getNombre());
        movimiento.setTipo(tipoMov);
        movimiento.setCantidad(cantidad);
        movimiento.setStockAnterior(stockAnterior);
        movimiento.setStockActual(stockNuevo);
        movimiento.setMotivo(motivo != null && motivo.isBlank() ? null : motivo);
        movimiento.setPedidoId(null);
        movimiento.setUsuario(usuarioActual());
        movimiento.setCreatedAt(LocalDateTime.now());
        movimientoRepository.save(movimiento);

        String accion = incrementa ? "entrada" : "salida";
        redirectAttributes.addFlashAttribute("mensaje",
                "Movimiento de " + accion + " registrado: \"" + producto.getNombre() + "\" (stock " + stockAnterior + " → " + stockNuevo + ").");
        return "redirect:/admin/inventario";
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getName() != null ? auth.getName() : "Sistema";
    }
}