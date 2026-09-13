package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ProductoResponse;
import com.olympic.olympic.entity.CarritoItem;
import com.olympic.olympic.service.CarritoService;
import com.olympic.olympic.service.ProductoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    private static final String SESSION_KEY = "carrito";

    private final CarritoService carritoService;
    private final ProductoService productoService;

    public CarritoController(CarritoService carritoService, ProductoService productoService) {
        this.carritoService = carritoService;
        this.productoService = productoService;
    }

    @PostMapping("/agregar")
    public String agregar(@RequestParam Integer productoId,
            @RequestParam(required = false) String talla,
            @RequestParam(required = false) String color,
            @RequestParam(defaultValue = "1") int cantidad,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {

        ProductoResponse producto = productoService.obtenerPorId(productoId);
        int stock = producto.getStock() != null ? producto.getStock() : 0;
        int solicitada = Math.max(cantidad, 1);

        List<CarritoItem> carrito = getCarrito(session);
        int yaEnCarrito = carrito.stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .mapToInt(CarritoItem::getCantidad)
                .sum();

        if (stock <= 0) {
            String error = "Lo sentimos, \"" + producto.getNombre() + "\" se agotó y ya no está disponible.";
            if (isAjax(requestedWith)) {
                return fragmentoCarrito(session, model, response, null, error);
            }
            redirectAttributes.addFlashAttribute("errorCarrito", error);
            return "redirect:/producto?id=" + productoId;
        }

        if (solicitada + yaEnCarrito > stock) {
            int disponibles = stock - yaEnCarrito;
            String error = disponibles <= 0
                    ? "Lo sentimos, \"" + producto.getNombre() + "\" se agotó y ya no está disponible."
                    : "Solo hay " + disponibles + " unidades disponibles de \"" + producto.getNombre() + "\".";
            if (isAjax(requestedWith)) {
                return fragmentoCarrito(session, model, response, null, error);
            }
            redirectAttributes.addFlashAttribute("errorCarrito", error);
            return "redirect:/producto?id=" + productoId;
        }

        CarritoItem item = new CarritoItem();
        item.setProductoId(producto.getId());
        item.setNombre(producto.getNombre());
        item.setImagenUrl(producto.getImagenUrl());
        item.setTalla(talla);
        item.setColor(color);
        item.setPrecioUnitario(
                producto.getPrecioPromocion() != null ? producto.getPrecioPromocion() : producto.getPrecio());
        item.setCantidad(solicitada);
        item.setDescuentoPorcentaje(producto.getDescuentoPorcentaje());

        carritoService.agregar(item, carrito);
        session.setAttribute(SESSION_KEY, carrito);

        String mensaje = "Producto agregado al carrito.";
        if (isAjax(requestedWith)) {
            return fragmentoCarrito(session, model, response, mensaje, null);
        }
        redirectAttributes.addFlashAttribute("mensajeCarrito", mensaje);
        return "redirect:" + rutaDeOrigen(request) + "#carrito";
    }

    @PostMapping("/actualizar/{index}")
    public String actualizarCantidad(@PathVariable int index,
            @RequestParam int cantidad,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {
        List<CarritoItem> carrito = getCarrito(session);
        String error = null;
        if (index >= 0 && index < carrito.size()) {
            CarritoItem item = carrito.get(index);
            if (cantidad > 0) {
                int stock = 0;
                try {
                    ProductoResponse producto = productoService.obtenerPorId(item.getProductoId());
                    stock = producto.getStock() != null ? producto.getStock() : 0;
                } catch (RuntimeException e) {
                    stock = 0;
                }
                if (stock <= 0) {
                    error = "Lo sentimos, \"" + item.getNombre() + "\" se agotó y ya no está disponible.";
                } else if (cantidad > stock) {
                    item.setCantidad(stock);
                    error = "Solo hay " + stock + " unidades disponibles de \"" + item.getNombre() + "\".";
                } else {
                    item.setCantidad(cantidad);
                }
            } else {
                carrito.remove(index);
            }
        }
        session.setAttribute(SESSION_KEY, carrito);

        if (isAjax(requestedWith)) {
            return fragmentoCarrito(session, model, response, null, error);
        }
        if (error != null) {
            redirectAttributes.addFlashAttribute("errorCarrito", error);
        }
        return "redirect:/#carrito";
    }

    @PostMapping("/eliminar/{index}")
    public String eliminar(@PathVariable int index,
            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            HttpServletResponse response,
            Model model,
            RedirectAttributes redirectAttributes) {
        List<CarritoItem> carrito = getCarrito(session);
        carritoService.eliminar(index, carrito);
        session.setAttribute(SESSION_KEY, carrito);

        if (isAjax(requestedWith)) {
            return fragmentoCarrito(session, model, response, null, null);
        }
        return "redirect:/#carrito";
    }

    @PostMapping("/limpiar")
    public String limpiar(@RequestHeader(value = "X-Requested-With", required = false) String requestedWith,
            HttpSession session,
            HttpServletResponse response,
            Model model) {
        List<CarritoItem> carrito = getCarrito(session);
        carritoService.limpiar(carrito);
        session.setAttribute(SESSION_KEY, carrito);

        if (isAjax(requestedWith)) {
            return fragmentoCarrito(session, model, response, null, null);
        }
        return "redirect:/";
    }

    /**
     * Devuelve el fragmento del sidebar del carrito renderizado, con el contador
     * también en la cabecera X-Carrito-Contador, para peticiones AJAX (evita
     * recargar la página completa y mantiene el carrito abierto).
     */
    private String fragmentoCarrito(HttpSession session, Model model, HttpServletResponse response,
            String mensaje, String error) {
        List<CarritoItem> carrito = getCarrito(session);
        model.addAttribute("carritoItems", carrito);
        model.addAttribute("carritoContador", carritoService.contarItems(carrito));
        model.addAttribute("carritoSubtotal", carritoService.calcularSubtotal(carrito));
        model.addAttribute("carritoDescuento", carritoService.calcularDescuentoTotal(carrito));
        model.addAttribute("carritoIVA", carritoService.calcularIVA(carrito));
        model.addAttribute("carritoTotal", carritoService.calcularTotal(carrito));
        model.addAttribute("mensajeCarrito", mensaje);
        model.addAttribute("errorCarrito", error);
        response.setHeader("X-Carrito-Contador", String.valueOf(carritoService.contarItems(carrito)));
        return "fragments/carrito-sidebar :: carritoSidebar";
    }

    private boolean isAjax(String requestedWith) {
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith);
    }

    @SuppressWarnings("unchecked")
    private List<CarritoItem> getCarrito(HttpSession session) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute(SESSION_KEY);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(SESSION_KEY, carrito);
        }
        return carrito;
    }

    /**
     * Ruta (path + query, sin host) de la página desde la que se hizo la petición,
     * tomada del header Referer. Si no hay Referer o no se puede leer, cae a "/".
     * Se descarta el host a propósito para no generar un redirect abierto hacia
     * otro dominio.
     */
    private String rutaDeOrigen(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer == null || referer.isBlank()) {
            return "/";
        }
        try {
            URI uri = new URI(referer);
            String path = uri.getRawPath();
            String query = uri.getRawQuery();
            String ruta = (path == null || path.isBlank()) ? "/" : path;
            return query != null ? ruta + "?" + query : ruta;
        } catch (URISyntaxException e) {
            return "/";
        }
    }
}