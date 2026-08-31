package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ProductoResponse;
import com.olympic.olympic.entity.CarritoItem;
import com.olympic.olympic.service.CarritoService;
import com.olympic.olympic.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
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
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {

        ProductoResponse producto = productoService.obtenerPorId(productoId);

        CarritoItem item = new CarritoItem();
        item.setProductoId(producto.getId());
        item.setNombre(producto.getNombre());
        item.setImagenUrl(producto.getImagenUrl());
        item.setTalla(talla);
        item.setColor(color);
        item.setPrecioUnitario(producto.getPrecioPromocion() != null ? producto.getPrecioPromocion() : producto.getPrecio());
        item.setCantidad(Math.max(cantidad, 1));
        item.setDescuentoPorcentaje(producto.getDescuentoPorcentaje());

        List<CarritoItem> carrito = getCarrito(session);
        carritoService.agregar(item, carrito);
        session.setAttribute(SESSION_KEY, carrito);

        redirectAttributes.addFlashAttribute("mensajeCarrito", "Producto agregado al carrito.");
        return "redirect:/#carrito";
    }

    @PostMapping("/actualizar/{index}")
    public String actualizarCantidad(@PathVariable int index,
                                     @RequestParam int cantidad,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        List<CarritoItem> carrito = getCarrito(session);
        carritoService.actualizarCantidad(index, cantidad, carrito);
        session.setAttribute(SESSION_KEY, carrito);
        return "redirect:/#carrito";
    }

    @PostMapping("/eliminar/{index}")
    public String eliminar(@PathVariable int index,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        List<CarritoItem> carrito = getCarrito(session);
        carritoService.eliminar(index, carrito);
        session.setAttribute(SESSION_KEY, carrito);
        return "redirect:/#carrito";
    }

    @PostMapping("/limpiar")
    public String limpiar(HttpSession session) {
        List<CarritoItem> carrito = getCarrito(session);
        carritoService.limpiar(carrito);
        session.setAttribute(SESSION_KEY, carrito);
        return "redirect:/";
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
}
