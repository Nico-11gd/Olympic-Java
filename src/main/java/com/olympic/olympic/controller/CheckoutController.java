package com.olympic.olympic.controller;

import com.olympic.olympic.dto.CheckoutRequest;
import com.olympic.olympic.entity.*;
import com.olympic.olympic.exception.RecursoNoEncontradoException;
import com.olympic.olympic.repository.*;
import com.olympic.olympic.service.CarritoService;
import com.olympic.olympic.service.QrService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Checkout completo y server-side: formulario de envío + método de pago,
 * validación de stock contra la BD, creación del pedido con su pago, su
 * detalle y los movimientos de inventario, y página de confirmación que
 * genera un QR real (ZXing) con los datos del pago.
 */
@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private static final String SESSION_KEY = "carrito";

    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;
    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PagoRepository pagoRepository;
    private final DireccionRepository direccionRepository;
    private final InventarioMovimientoRepository inventarioMovimientoRepository;
    private final QrService qrService;

    public CheckoutController(CarritoService carritoService,
                              UsuarioRepository usuarioRepository,
                              ProductoRepository productoRepository,
                              PedidoRepository pedidoRepository,
                              DetallePedidoRepository detallePedidoRepository,
                              PagoRepository pagoRepository,
                              DireccionRepository direccionRepository,
                              InventarioMovimientoRepository inventarioMovimientoRepository,
                              QrService qrService) {
        this.carritoService = carritoService;
        this.usuarioRepository = usuarioRepository;
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.pagoRepository = pagoRepository;
        this.direccionRepository = direccionRepository;
        this.inventarioMovimientoRepository = inventarioMovimientoRepository;
        this.qrService = qrService;
    }

    @SuppressWarnings("unchecked")
    @ModelAttribute
    public void inyectarCarrito(HttpSession session, Model model) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute(SESSION_KEY);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(SESSION_KEY, carrito);
        }
        model.addAttribute("carritoItems", carrito);
        model.addAttribute("carritoContador", carritoService.contarItems(carrito));
        model.addAttribute("carritoSubtotal", carritoService.calcularSubtotal(carrito));
        model.addAttribute("carritoDescuento", carritoService.calcularDescuentoTotal(carrito));
        model.addAttribute("carritoIVA", carritoService.calcularIVA(carrito));
        model.addAttribute("carritoTotal", carritoService.calcularTotal(carrito));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            model.addAttribute("usuarioActual", usuarioRepository.findByCorreo(auth.getName()).orElse(null));
        } else {
            model.addAttribute("usuarioActual", null);
        }
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MetodoPago.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String texto) {
                setValue(texto == null || texto.isBlank() ? null : MetodoPago.fromValor(texto));
            }
        });
    }

    @GetMapping
    public String mostrarCheckout(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioAutenticado();
        if (usuario == null) {
            return "redirect:/login";
        }

        List<CarritoItem> carrito = carrito(session);
        if (carrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tu carrito está vacío.");
            return "redirect:/coleccion";
        }

        if (!model.containsAttribute("checkout")) {
            CheckoutRequest request = new CheckoutRequest();
            request.setNombreEnvio(usuario.getNombre());
            request.setMetodo(MetodoPago.NEQUI);

            List<Direccion> direcciones = direccionesDe(usuario);
            Direccion principal = direcciones.stream()
                    .filter(d -> Boolean.TRUE.equals(d.getEsPrincipal()))
                    .findFirst()
                    .orElse(direcciones.isEmpty() ? null : direcciones.get(0));
            if (principal != null) {
                request.setTelefono(principal.getTelefono());
                request.setDepartamento(principal.getDepartamento());
                request.setCiudad(principal.getCiudad());
                request.setDireccion(principal.getDireccion());
                request.setReferencias(principal.getReferencias());
            }
            model.addAttribute("checkout", request);
        }

        model.addAttribute("direcciones", direccionesDe(usuario));
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("etiquetasMetodo", etiquetasMetodo());
        model.addAttribute("iconosMetodo", iconosMetodo());
        return "checkout";
    }

    private Map<MetodoPago, String> etiquetasMetodo() {
        Map<MetodoPago, String> map = new HashMap<>();
        map.put(MetodoPago.EFECTIVO, "Efectivo");
        map.put(MetodoPago.NEQUI, "Nequi");
        map.put(MetodoPago.DAVIPLATA, "Daviplata");
        map.put(MetodoPago.TRANSFERENCIA, "Transferencia");
        map.put(MetodoPago.TARJETA, "Tarjeta / Débito");
        return map;
    }

    private Map<MetodoPago, String> iconosMetodo() {
        Map<MetodoPago, String> map = new HashMap<>();
        map.put(MetodoPago.EFECTIVO, "bi-cash");
        map.put(MetodoPago.NEQUI, "bi-phone");
        map.put(MetodoPago.DAVIPLATA, "bi-wallet2");
        map.put(MetodoPago.TRANSFERENCIA, "bi-bank");
        map.put(MetodoPago.TARJETA, "bi-credit-card");
        return map;
    }

    @PostMapping
    @Transactional
    public String procesarCheckout(@Valid @ModelAttribute("checkout") CheckoutRequest request,
                                   BindingResult resultado,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioAutenticado();
        if (usuario == null) {
            return "redirect:/login";
        }

        List<CarritoItem> carrito = carrito(session);
        if (carrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tu carrito está vacío.");
            return "redirect:/coleccion";
        }

        String errorStock = validarStock(carrito);
        if (errorStock != null) {
            redirectAttributes.addFlashAttribute("errorCarrito", errorStock);
            return "redirect:/#carrito";
        }

        if (resultado.hasErrors()) {
            model.addAttribute("direcciones", direccionesDe(usuario));
            model.addAttribute("metodosPago", MetodoPago.values());
            model.addAttribute("etiquetasMetodo", etiquetasMetodo());
            model.addAttribute("iconosMetodo", iconosMetodo());
            return "checkout";
        }

        BigDecimal total = carritoService.calcularTotal(carrito);

        Pedido pedido = new Pedido();
        pedido.setClienteId(usuario.getId());
        pedido.setTotal(total);
        pedido.setEstado("pendiente");
        pedido.setNombreEnvio(request.getNombreEnvio());
        pedido.setDireccion(request.getDireccion());
        pedido.setCiudad(request.getCiudad());
        pedido.setNotasEnvio(notasEnvio(request));
        pedido.setCreatedAt(LocalDateTime.now());
        pedidoRepository.save(pedido);

        for (CarritoItem item : carrito) {
            DetallePedido detalle = new DetallePedido();
            detalle.setPedido(pedido);
            detalle.setProductoId(item.getProductoId());
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(item.getPrecioUnitario());
            detallePedidoRepository.save(detalle);

            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
            int stockAnterior = producto.getStock() != null ? producto.getStock() : 0;
            producto.setStock(Math.max(stockAnterior - item.getCantidad(), 0));
            productoRepository.save(producto);

            InventarioMovimiento movimiento = new InventarioMovimiento();
            movimiento.setProductoId(producto.getId());
            movimiento.setProductoNombre(producto.getNombre());
            movimiento.setTipo(TipoMovimiento.VENTA);
            movimiento.setCantidad(item.getCantidad());
            movimiento.setStockAnterior(stockAnterior);
            movimiento.setStockActual(producto.getStock());
            movimiento.setMotivo("Venta - pedido #" + pedido.getId());
            movimiento.setPedidoId(pedido.getId());
            movimiento.setUsuario(usuario.getNombre());
            movimiento.setCreatedAt(LocalDateTime.now());
            inventarioMovimientoRepository.save(movimiento);
        }

        Pago pago = new Pago();
        pago.setPedido(pedido);
        pago.setMetodo(request.getMetodo() != null ? request.getMetodo() : MetodoPago.EFECTIVO);
        pago.setEstado(EstadoPago.PENDIENTE);
        pago.setTotal(total);
        pago.setActivo(true);
        pago.setSubtotal(carritoService.calcularSubtotal(carrito).subtract(carritoService.calcularDescuentoTotal(carrito)));
        pago.setDescuento(carritoService.calcularDescuentoTotal(carrito));
        pago.setIva(carritoService.calcularIVA(carrito));
        pago.setMetodoPago(pago.getMetodo().getValor());
        pago.setNombre(usuario.getNombre());
        pago.setNumero("PEDIDO-" + pedido.getId());
        pago.setReferencia("Pedido #" + pedido.getId());
        pago.setUsuarioId(usuario.getId());
        pago.setClienteCorreo(usuario.getCorreo());
        pago.setCiudad(request.getCiudad());
        pago.setDireccion(request.getDireccion());
        pago.setTelefono(request.getTelefono());
        pago.setCreatedAt(LocalDateTime.now());
        pagoRepository.save(pago);

        guardarDireccion(request, usuario);

        session.setAttribute(SESSION_KEY, new ArrayList<CarritoItem>());
        return "redirect:/checkout/confirmacion?pedido=" + pedido.getId();
    }

    @GetMapping("/confirmacion")
    public String confirmacion(@RequestParam("pedido") Integer pedidoId,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Usuario usuario = usuarioAutenticado();
        if (usuario == null) {
            return "redirect:/login";
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido no encontrado."));
        if (!usuario.getId().equals(pedido.getClienteId())) {
            redirectAttributes.addFlashAttribute("error", "Este pedido no te pertenece.");
            return "redirect:/cliente";
        }

        Pago pago = pagoRepository.findTopByPedidoIdOrderByIdDesc(pedidoId).orElse(null);
        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoIdOrderByIdAsc(pedidoId);

        Map<Integer, Producto> productos = new HashMap<>();
        for (DetallePedido d : detalles) {
            productoRepository.findById(d.getProductoId()).ifPresent(p -> productos.put(d.getProductoId(), p));
        }

        model.addAttribute("pedido", pedido);
        model.addAttribute("pago", pago);
        model.addAttribute("detalles", detalles);
        model.addAttribute("productos", productos);
        model.addAttribute("qrVisible", pago != null && pago.getMetodo() != MetodoPago.EFECTIVO);
        return "checkout-confirmacion";
    }

    @GetMapping("/qr/{pedidoId}")
    public ResponseEntity<byte[]> qr(@PathVariable Integer pedidoId) {
        Pago pago = pagoRepository.findTopByPedidoIdOrderByIdDesc(pedidoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado."));
        String contenido = qrService.contenidoPago(
                pago.getMetodo(), pago.getPedido().getId(), pago.getTotal());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrService.generarPng(contenido, 320, 320));
    }

    private String validarStock(List<CarritoItem> carrito) {
        Map<Integer, Integer> porProducto = new HashMap<>();
        for (CarritoItem item : carrito) {
            porProducto.merge(item.getProductoId(), item.getCantidad(), Integer::sum);
        }
        for (Map.Entry<Integer, Integer> entrada : porProducto.entrySet()) {
            Producto producto = productoRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
            int stock = producto.getStock() != null ? producto.getStock() : 0;
            if (entrada.getValue() > stock) {
                if (stock <= 0) {
                    return "Lo sentimos, \"" + producto.getNombre() + "\" se agotó y ya no está disponible.";
                }
                return "Lo sentimos, solo hay " + stock + " unidades disponibles de \""
                        + producto.getNombre() + "\" y pediste " + entrada.getValue() + ".";
            }
        }
        return null;
    }

    private String notasEnvio(CheckoutRequest request) {
        String notas = (request.getDepartamento() != null ? request.getDepartamento() : "")
                + " · Tel: " + (request.getTelefono() != null ? request.getTelefono() : "");
        if (request.getReferencias() != null && !request.getReferencias().isBlank()) {
            notas += " · Ref: " + request.getReferencias();
        }
        return notas;
    }

    private void guardarDireccion(CheckoutRequest request, Usuario usuario) {
        if (request.getDireccion() == null || request.getDireccion().isBlank()) {
            return;
        }
        Direccion direccion = new Direccion();
        direccion.setUsuario(usuario);
        direccion.setNombreDestinatario(request.getNombreEnvio());
        direccion.setTelefono(request.getTelefono());
        direccion.setDepartamento(request.getDepartamento());
        direccion.setCiudad(request.getCiudad());
        direccion.setDireccion(request.getDireccion());
        direccion.setReferencias(request.getReferencias());
        direccion.setEsPrincipal(false);
        direccion.setActivo(true);
        direccion.setCreatedAt(LocalDateTime.now());
        direccionRepository.save(direccion);
    }

    private List<Direccion> direccionesDe(Usuario usuario) {
        return direccionRepository.findByUsuarioIdAndActivoTrueOrderByEsPrincipalDescIdDesc(usuario.getId());
    }

    private Usuario usuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return usuarioRepository.findByCorreo(auth.getName()).orElse(null);
    }

    @SuppressWarnings("unchecked")
    private List<CarritoItem> carrito(HttpSession session) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute(SESSION_KEY);
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute(SESSION_KEY, carrito);
        }
        return carrito;
    }
}