package com.olympic.olympic.controller;

import com.olympic.olympic.dto.ProductoResponse;
import com.olympic.olympic.dto.RegistroRequest;
import com.olympic.olympic.entity.CarritoItem;
import com.olympic.olympic.entity.DetallePedido;
import com.olympic.olympic.entity.EstadoPago;
import com.olympic.olympic.entity.Pago;
import com.olympic.olympic.entity.Pedido;
import com.olympic.olympic.entity.Producto;
import com.olympic.olympic.entity.Rol;
import com.olympic.olympic.entity.Usuario;
import com.olympic.olympic.exception.RecursoDuplicadoException;
import com.olympic.olympic.repository.CategoriaRepository;
import com.olympic.olympic.repository.DetallePedidoRepository;
import com.olympic.olympic.repository.PagoRepository;
import com.olympic.olympic.repository.PedidoRepository;
import com.olympic.olympic.repository.ProductoRepository;
import com.olympic.olympic.repository.UsuarioRepository;
import com.olympic.olympic.service.AuthService;
import com.olympic.olympic.service.CarritoService;
import com.olympic.olympic.service.ProductoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Páginas públicas (catálogo, detalle de producto, login, registro) y la
 * entrada del panel admin. Todo server-side con Thymeleaf: nada de fetch()
 * ni JSON, cada botón es un <a> o un <form method="post">.
 */
@Controller
public class PageController {

    private final ProductoService productoService;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final AuthService authService;
    private final CarritoService carritoService;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final PagoRepository pagoRepository;

    public PageController(ProductoService productoService,
                           CategoriaRepository categoriaRepository,
                           ProductoRepository productoRepository,
                           AuthService authService,
                           CarritoService carritoService,
                           UsuarioRepository usuarioRepository,
                           PedidoRepository pedidoRepository,
                           DetallePedidoRepository detallePedidoRepository,
                           PagoRepository pagoRepository) {
        this.productoService = productoService;
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
        this.authService = authService;
        this.carritoService = carritoService;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.pagoRepository = pagoRepository;
    }

    @SuppressWarnings("unchecked")
    @ModelAttribute
    public void injectarCarrito(HttpSession session, Model model) {
        List<CarritoItem> carrito = (List<CarritoItem>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        model.addAttribute("carritoItems", carrito);
        model.addAttribute("carritoContador", carritoService.contarItems(carrito));
        model.addAttribute("carritoSubtotal", carritoService.calcularSubtotal(carrito));
        model.addAttribute("carritoDescuento", carritoService.calcularDescuentoTotal(carrito));
        model.addAttribute("carritoIVA", carritoService.calcularIVA(carrito));
        model.addAttribute("carritoTotal", carritoService.calcularTotal(carrito));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String correo = auth.getName();
            Usuario usuarioActual = usuarioRepository.findByCorreo(correo).orElse(null);
            model.addAttribute("usuarioActual", usuarioActual);
        } else {
            model.addAttribute("usuarioActual", null);
        }
    }

    // Catálogo público (equivalente a app/(tabs)/catalogo.tsx): cualquiera lo ve,
    // con o sin sesión. Búsqueda y filtro por categoría vía query params
    // (?q=...&categoria=...), resueltos aquí mismo, sin JavaScript.
    @GetMapping("/")
    public String raiz(@RequestParam(name = "q", required = false) String q,
                        @RequestParam(name = "categoria", required = false) Integer categoriaId,
                        Model model) {

        List<ProductoResponse> productos = productoService.listar(false);

        if (q != null && !q.isBlank()) {
            String buscado = q.trim().toLowerCase();
            productos = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(buscado))
                    .toList();
        }

        if (categoriaId != null) {
            productos = productos.stream()
                    .filter(p -> categoriaId.equals(p.getCategoriaId()))
                    .toList();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("busqueda", q);
        return "inicio";
    }

    // Colección: vista independiente del catálogo con filtros, buscador y
    // todos los productos, a la que se llega desde "VER COLECCIÓN" en la raíz.
    @GetMapping("/coleccion")
    public String coleccion(@RequestParam(name = "q", required = false) String q,
                            @RequestParam(name = "categoria", required = false) Integer categoriaId,
                            Model model) {

        List<ProductoResponse> productos = productoService.listar(false);

        if (q != null && !q.isBlank()) {
            String buscado = q.trim().toLowerCase();
            productos = productos.stream()
                    .filter(p -> p.getNombre().toLowerCase().contains(buscado))
                    .toList();
        }

        if (categoriaId != null) {
            productos = productos.stream()
                    .filter(p -> categoriaId.equals(p.getCategoriaId()))
                    .toList();
        }

        model.addAttribute("productos", productos);
        model.addAttribute("categorias", categoriaRepository.findByActivoTrueOrderByNombreAsc());
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("busqueda", q);
        return "coleccion";
    }

    // Detalle de un producto (equivalente a app/producto/[id].tsx).
    @GetMapping("/producto")
    public String producto(@RequestParam("id") Integer id,
                           @RequestParam(name = "qty", required = false, defaultValue = "1") Integer qty,
                           Model model, RedirectAttributes redirectAttributes) {
        try {
            ProductoResponse producto = productoService.obtenerPorId(id);
            model.addAttribute("producto", producto);

            int cantidad = (qty != null && qty >= 1) ? Math.min(qty, Math.max(producto.getStock(), 1)) : 1;
            model.addAttribute("cantidadSeleccionada", cantidad);

            List<ProductoResponse> relacionados = productoRepository
                    .findByCategoriaIdAndActivoTrueOrderByIdDesc(producto.getCategoriaId())
                    .stream()
                    .filter(p -> !p.getId().equals(id))
                    .limit(4)
                    .map(ProductoResponse::fromEntity)
                    .toList();
            model.addAttribute("relacionados", relacionados);

            return "producto";
        } catch (com.olympic.olympic.exception.RecursoNoEncontradoException e) {
            redirectAttributes.addFlashAttribute("error", "Ese producto ya no está disponible.");
            return "redirect:/";
        }
    }

    @GetMapping("/login")
    public String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && !(auth instanceof AnonymousAuthenticationToken)) {
            return "redirect:" + homePorRol(auth);
        }
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        if (!model.containsAttribute("registro")) {
            model.addAttribute("registro", new RegistroRequest());
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registro") RegistroRequest registro,
                                    BindingResult resultado,
                                    RedirectAttributes redirectAttributes,
                                    Model model) {
        if (resultado.hasErrors()) {
            return "registro";
        }

        try {
            authService.registrar(registro);
        } catch (RecursoDuplicadoException e) {
            model.addAttribute("error", e.getMessage());
            return "registro";
        }

        redirectAttributes.addFlashAttribute("mensaje", "Cuenta creada. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }

    // Dashboard (home por rol). Ahora es accesible por cualquier usuario
    // autenticado; su contenido y el menú lateral se adaptan al rol:
    //   - ADMIN: métricas de la tienda (productos, stock, usuarios, categorías).
    //   - CLIENTE: su propia información de cuenta.
    @GetMapping("/admin")
    public String adminInicio(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean esAdmin = esAdministrador(auth);

        if (esAdmin) {
            List<Producto> todos = productoRepository.findAll();
            long totalProductos = todos.size();
            long activos = todos.stream().filter(p -> Boolean.TRUE.equals(p.getActivo())).count();
            long inactivos = totalProductos - activos;
            long stockTotal = todos.stream().mapToLong(Producto::getStock).sum();
            long stockBajo = todos.stream().filter(p -> p.getStock() != null && p.getStock() <= 5).count();

            List<Usuario> usuarios = usuarioRepository.findAll();
            long totalUsuarios = usuarios.size();
            long totalAdmins = usuarios.stream().filter(u -> u.getRol() == Rol.ADMIN).count();
            long totalClientes = usuarios.stream().filter(u -> u.getRol() == Rol.CLIENTE).count();
            long categorias = categoriaRepository.findByActivoTrueOrderByNombreAsc().size();

            model.addAttribute("esAdmin", true);
            model.addAttribute("totalProductos", totalProductos);
            model.addAttribute("productosActivos", activos);
            model.addAttribute("productosInactivos", inactivos);
            model.addAttribute("stockTotal", stockTotal);
            model.addAttribute("stockBajo", stockBajo);
            model.addAttribute("totalUsuarios", totalUsuarios);
            model.addAttribute("totalAdmins", totalAdmins);
            model.addAttribute("totalClientes", totalClientes);
            model.addAttribute("totalCategorias", categorias);
        } else {
            model.addAttribute("esAdmin", false);
            Usuario cliente = clienteAutenticado();
            model.addAttribute("cliente", cliente);
            if (cliente != null) {
                cargarDatosCliente(model, cliente);
            }
        }
        return "admin/inicio";
    }

    // ── Páginas del cliente fuera del dashboard principal ────────────────
    // El home del cliente es un dashboard; el detalle queda en páginas aparte
    // (/cliente/perfil, /cliente/compras, /cliente/pagos) enlazadas desde la
    // barra lateral y los accesos rápidos del panel.
    @GetMapping("/cliente/perfil")
    public String clientePerfil(Model model) {
        Usuario cliente = clienteAutenticado();
        if (cliente == null) {
            return "redirect:/admin";
        }
        model.addAttribute("cliente", cliente);
        cargarDatosCliente(model, cliente);
        model.addAttribute("currentPage", "perfilCliente");
        return "cliente/perfil";
    }

    @GetMapping("/cliente/compras")
    public String clienteCompras(Model model) {
        Usuario cliente = clienteAutenticado();
        if (cliente == null) {
            return "redirect:/admin";
        }
        model.addAttribute("esAdmin", false);
        model.addAttribute("cliente", cliente);
        cargarDatosCliente(model, cliente);
        model.addAttribute("currentPage", "comprasCliente");
        return "cliente/compras";
    }

    @GetMapping("/cliente/pagos")
    public String clientePagos(Model model) {
        Usuario cliente = clienteAutenticado();
        if (cliente == null) {
            return "redirect:/admin";
        }
        model.addAttribute("esAdmin", false);
        model.addAttribute("cliente", cliente);
        cargarDatosCliente(model, cliente);
        model.addAttribute("currentPage", "pagosCliente");
        return "cliente/pagos";
    }

    private Usuario clienteAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth != null ? auth.getName() : null;
        return (correo != null) ? usuarioRepository.findByCorreo(correo).orElse(null) : null;
    }

    // Carga los datos de cuenta del cliente (pedidos con items, pagos y totales)
    // compartidos entre el dashboard y las páginas /cliente/*.
    private void cargarDatosCliente(Model model, Usuario cliente) {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByCreatedAtDesc(cliente.getId());
        List<DetallePedido> detalles = pedidos.isEmpty()
                ? List.of()
                : pedidos.stream()
                        .flatMap(p -> detallePedidoRepository.findByPedidoIdOrderByIdAsc(p.getId()).stream())
                        .toList();
        Map<Integer, Producto> productosPorId = detalles.stream()
                .map(DetallePedido::getProductoId)
                .distinct()
                .map(id -> productoRepository.findById(id).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Producto::getId, p -> p, (a, b) -> a));

        List<PedidoView> misPedidos = pedidos.stream()
                .map(p -> {
                    List<ItemView> items = detallePedidoRepository.findByPedidoIdOrderByIdAsc(p.getId()).stream()
                            .map(d -> new ItemView(d, productosPorId.get(d.getProductoId())))
                            .toList();
                    Pago pago = pagoRepository.findTopByPedidoIdOrderByIdDesc(p.getId()).orElse(null);
                    return new PedidoView(p, items, pago);
                })
                .toList();

        List<Pago> misPagos = pagoRepository.findByUsuarioIdOrderByIdDesc(cliente.getId());
        long pagosPendientes = misPagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.PENDIENTE)
                .count();

        BigDecimal totalInvertido = misPedidos.stream()
                .map(PedidoView::pedido)
                .map(Pedido::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long pedidosEntregados = misPedidos.stream()
                .map(PedidoView::pedido)
                .filter(p -> "entregado".equalsIgnoreCase(p.getEstado()))
                .count();
        long pedidosEnProceso = misPedidos.stream()
                .map(PedidoView::pedido)
                .filter(p -> p.getEstado() != null
                        && !"entregado".equalsIgnoreCase(p.getEstado())
                        && !"cancelado".equalsIgnoreCase(p.getEstado()))
                .count();

        model.addAttribute("misPedidos", misPedidos);
        model.addAttribute("misPagos", misPagos);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("totalInvertido", totalInvertido);
        model.addAttribute("pedidosEntregados", pedidosEntregados);
        model.addAttribute("pedidosEnProceso", pedidosEnProceso);

        long pagosAprobados = misPagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.APROBADO)
                .count();
        long pagosRechazados = misPagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.RECHAZADO)
                .count();
        BigDecimal montoAprobado = misPagos.stream()
                .filter(p -> p.getEstado() == EstadoPago.APROBADO)
                .map(Pago::getTotal)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("pagosAprobados", pagosAprobados);
        model.addAttribute("pagosRechazados", pagosRechazados);
        model.addAttribute("montoAprobado", montoAprobado);
    }

    private String homePorRol(Authentication auth) {
        return esAdministrador(auth) ? "/admin" : "/admin";
    }

    private boolean esAdministrador(Authentication auth) {
        if (auth == null) {
            return false;
        }
        for (GrantedAuthority autoridad : auth.getAuthorities()) {
            if ("ROLE_ADMIN".equals(autoridad.getAuthority())) {
                return true;
            }
        }
        return false;
    }

    // ── Vistas auxiliares para "mis compras" del cliente ─────────────────
    public record ItemView(DetallePedido detalle, Producto producto) {}

    public record PedidoView(Pedido pedido, List<ItemView> items, Pago pago) {}
}
