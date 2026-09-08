package com.olympic.olympic.controller;

import com.olympic.olympic.dto.PagoResponse;
import com.olympic.olympic.entity.DetallePedido;
import com.olympic.olympic.entity.EstadoPago;
import com.olympic.olympic.entity.InventarioMovimiento;
import com.olympic.olympic.entity.MetodoPago;
import com.olympic.olympic.entity.Producto;
import com.olympic.olympic.entity.TipoMovimiento;
import com.olympic.olympic.repository.CategoriaRepository;
import com.olympic.olympic.repository.DetallePedidoRepository;
import com.olympic.olympic.repository.InventarioMovimientoRepository;
import com.olympic.olympic.repository.ProductoRepository;
import com.olympic.olympic.service.ConfiguracionService;
import com.olympic.olympic.service.PagoService;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Reportes del panel admin (acceso ADMIN). Cada reporte se renderiza como
 * página imprimible (botón Imprimir -> PDF) con el logo de la empresa, NIT,
 * teléfono y correo tomados de la tabla `configuracion`, tablas de detalle y
 * gráficos con Chart.js. Todo server-side: los datos se precalculan aquí.
 */
@Controller
@RequestMapping("/admin/reportes")
public class AdminReporteController {

    private final ConfiguracionService configuracionService;
    private final PagoService pagoService;
    private final ProductoRepository productoRepository;
    private final InventarioMovimientoRepository movimientoRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final CategoriaRepository categoriaRepository;

    private static final int STOCK_MINIMO = 5;

    public AdminReporteController(ConfiguracionService configuracionService,
                                  PagoService pagoService,
                                  ProductoRepository productoRepository,
                                  InventarioMovimientoRepository movimientoRepository,
                                  DetallePedidoRepository detallePedidoRepository,
                                  CategoriaRepository categoriaRepository) {
        this.configuracionService = configuracionService;
        this.pagoService = pagoService;
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    @GetMapping
    public String indice(Model model) {
        prepararBase(model, "Reportes");
        model.addAttribute("totalPagos", pagoService.listar().size());
        model.addAttribute("totalProductos", productoRepository.findAll().size());
        model.addAttribute("movimientos", movimientoRepository.findAll().size());
        model.addAttribute("stockBajo", productoRepository.findByActivoTrueOrderByNombreAsc().stream()
                .filter(p -> p.getStock() != null && p.getStock() <= STOCK_MINIMO).count());
        return "admin/reportes/inicio";
    }

    // ── REPORTE DE PAGOS ──────────────────────────────────────────────────
    @GetMapping("/pagos")
    public String reportePagos(@RequestParam(name = "desde", required = false) String desde,
                               @RequestParam(name = "hasta", required = false) String hasta,
                               @RequestParam(name = "estado", required = false, defaultValue = "todos") String estado,
                               @RequestParam(name = "metodo", required = false, defaultValue = "todos") String metodo,
                               Model model) {

        prepararBase(model, "Reporte de pagos");

        List<PagoResponse> pagos = filtrarPagos(desde, hasta, estado, metodo);

        // Totales por estado
        BigDecimal totalGeneral = sumar(pagos, PagoResponse::getTotal);
        PagosResumen resumen = resumenPagos(pagos);

        // Serie diaria (barras)
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<String, BigDecimal> porDia = new LinkedHashMap<>();
        for (PagoResponse p : pagos) {
            String clave = p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate().format(df) : "Sin fecha";
            porDia.merge(clave, p.getTotal(), BigDecimal::add);
        }
        List<Map.Entry<String, BigDecimal>> dias = new ArrayList<>(porDia.entrySet());
        dias.sort(Map.Entry.comparingByKey());
        List<String> labelsDias = dias.stream().map(Map.Entry::getKey).toList();
        List<BigDecimal> valoresDias = dias.stream().map(Map.Entry::getValue).toList();

        // Por método (dona)
        Map<String, BigDecimal> porMetodo = new LinkedHashMap<>();
        for (PagoResponse p : pagos) {
            String clave = p.getMetodo() != null ? capitalize(p.getMetodo().getValor()) : "Otro";
            porMetodo.merge(clave, p.getTotal(), BigDecimal::add);
        }

        model.addAttribute("pagos", pagos);
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("resumen", resumen);
        model.addAttribute("labelsDias", labelsDias);
        model.addAttribute("valoresDias", valoresDias);
        model.addAttribute("labelsMetodo", new ArrayList<>(porMetodo.keySet()));
        model.addAttribute("valoresMetodo", new ArrayList<>(porMetodo.values()));
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("metodoSeleccionado", metodo);
        model.addAttribute("desdeStr", desde != null ? desde : "");
        model.addAttribute("hastaStr", hasta != null ? hasta : "");
        model.addAttribute("metodosPago", MetodoPago.values());
        model.addAttribute("estadosPago", EstadoPago.values());

        return "admin/reportes/pagos";
    }

    // ── REPORTE DE PRODUCTOS ──────────────────────────────────────────────
    @GetMapping("/productos")
    public String reporteProductos(@RequestParam(name = "categoria", required = false) Integer categoriaId,
                                   @RequestParam(name = "estado", required = false, defaultValue = "todos") String estado,
                                   @RequestParam(name = "q", required = false) String q,
                                   Model model) {

        prepararBase(model, "Reporte de productos");

        List<Producto> productos = filtrarProductos(categoriaId, estado, q);

        // Unidades vendidas por producto (desde el detalle de pedidos)
        Map<Integer, Integer> vendidas = detallePedidoRepository.findAll().stream()
                .collect(Collectors.groupingBy(DetallePedido::getProductoId,
                        Collectors.summingInt(DetallePedido::getCantidad)));

        BigDecimal valorInventario = BigDecimal.ZERO;
        long stockTotal = 0;
        long stockBajo = 0;
        for (Producto p : productos) {
            int stock = p.getStock() != null ? p.getStock() : 0;
            stockTotal += stock;
            valorInventario = valorInventario.add(precioVenta(p).multiply(BigDecimal.valueOf(stock)));
            if (stock <= STOCK_MINIMO) {
                stockBajo++;
            }
        }

        // Valor de inventario por categoría (barras)
        Map<String, BigDecimal> porCategoria = new LinkedHashMap<>();
        for (Producto p : productos) {
            String clave = p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin categoría";
            porCategoria.merge(clave,
                    precioVenta(p).multiply(BigDecimal.valueOf(p.getStock() != null ? p.getStock() : 0)),
                    BigDecimal::add);
        }

        // Unidades vendidas top 10 (barras)
        List<Map.Entry<Integer, Integer>> top = vendidas.entrySet().stream()
                .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
                .limit(10)
                .toList();
        List<String> topLabels = top.stream()
                .map(e -> productoNombre(e.getKey()))
                .toList();
        List<Integer> topValores = top.stream().map(Map.Entry::getValue).toList();

        model.addAttribute("productos", productos);
        model.addAttribute("vendidas", vendidas);
        model.addAttribute("totalProductos", productos.size());
        model.addAttribute("stockTotal", stockTotal);
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("valorInventario", valorInventario);
        model.addAttribute("labelsCategoria", new ArrayList<>(porCategoria.keySet()));
        model.addAttribute("valoresCategoria", new ArrayList<>(porCategoria.values()));
        model.addAttribute("labelsTop", topLabels);
        model.addAttribute("valoresTop", topValores);
        model.addAttribute("categoriaSeleccionada", categoriaId);
        model.addAttribute("estadoSeleccionado", estado);
        model.addAttribute("busqueda", q);
        model.addAttribute("categorias", categoriaRepository.findByActivoTrueOrderByNombreAsc());
        return "admin/reportes/productos";
    }

    // ── REPORTE DE INVENTARIO ─────────────────────────────────────────────
    @GetMapping("/inventario")
    public String reporteInventario(@RequestParam(name = "desde", required = false) String desde,
                                    @RequestParam(name = "hasta", required = false) String hasta,
                                    @RequestParam(name = "tipo", required = false, defaultValue = "todos") String tipo,
                                    Model model) {

        prepararBase(model, "Reporte de inventario");

        List<InventarioMovimiento> movimientos = filtrarMovimientos(desde, hasta, tipo);

        long entradas = 0;
        long salidas = 0;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        Map<String, long[]> porDia = new LinkedHashMap<>();
        Map<String, Long> porTipo = new LinkedHashMap<>();
        for (InventarioMovimiento m : movimientos) {
            int cantidad = m.getCantidad() != null ? m.getCantidad() : 0;
            boolean incrementa = incrementa(m.getTipo(), cantidad);
            long[] dia = porDia.computeIfAbsent(
                    m.getCreatedAt() != null ? m.getCreatedAt().toLocalDate().format(df) : "Sin fecha",
                    k -> new long[]{0, 0});
            if (incrementa) {
                entradas += Math.abs(cantidad);
                dia[0] += Math.abs(cantidad);
            } else {
                salidas += Math.abs(cantidad);
                dia[1] += Math.abs(cantidad);
            }
            String tipoLabel = m.getTipo() != null ? capitalize(m.getTipo().getValor()) : "Otro";
            porTipo.merge(tipoLabel, 1L, Long::sum);
        }

        List<String> labelsDias = porDia.keySet().stream().sorted().toList();
        List<Long> entradasDias = labelsDias.stream().map(k -> porDia.get(k)[0]).toList();
        List<Long> salidasDias = labelsDias.stream().map(k -> porDia.get(k)[1]).toList();

        // Resumen de stock actual por producto
        List<Producto> productos = productoRepository.findAllByOrderByIdDesc();
        long stockTotal = productos.stream().mapToLong(p -> p.getStock() != null ? p.getStock() : 0).sum();
        long stockBajo = productos.stream().filter(p -> p.getStock() != null && p.getStock() <= STOCK_MINIMO).count();
        BigDecimal valorInventario = productos.stream()
                .map(p -> precioVenta(p).multiply(BigDecimal.valueOf(p.getStock() != null ? p.getStock() : 0)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("movimientos", movimientos);
        model.addAttribute("productos", productos);
        model.addAttribute("totalEntradas", entradas);
        model.addAttribute("totalSalidas", salidas);
        model.addAttribute("stockTotal", stockTotal);
        model.addAttribute("stockBajo", stockBajo);
        model.addAttribute("valorInventario", valorInventario);
        model.addAttribute("labelsDias", labelsDias);
        model.addAttribute("entradasDias", entradasDias);
        model.addAttribute("salidasDias", salidasDias);
        model.addAttribute("labelsTipo", new ArrayList<>(porTipo.keySet()));
        model.addAttribute("valoresTipo", new ArrayList<>(porTipo.values()));
        model.addAttribute("tipoSeleccionado", tipo);
        model.addAttribute("desdeStr", desde != null ? desde : "");
        model.addAttribute("hastaStr", hasta != null ? hasta : "");
        model.addAttribute("tiposMovimiento", List.of(
                TipoMovimiento.ENTRADA, TipoMovimiento.VENTA, TipoMovimiento.DEVOLUCION,
                TipoMovimiento.DANO, TipoMovimiento.PERDIDA, TipoMovimiento.AJUSTE));
        return "admin/reportes/inventario";
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private void prepararBase(Model model, String titulo) {
        model.addAttribute("emp", configuracionService.mapa());
        model.addAttribute("tituloReporte", titulo);
        model.addAttribute("usuarioReporte", usuarioActual());
        model.addAttribute("fechaGeneracion", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getName() != null ? auth.getName() : "Sistema";
    }

    private LocalDate parseFecha(String fecha) {
        if (fecha == null || fecha.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(fecha);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean dentroDeRango(java.time.LocalDateTime cuando, LocalDate desde, LocalDate hasta) {
        if (cuando == null) {
            return false;
        }
        if (desde != null && cuando.isBefore(desde.atStartOfDay())) {
            return false;
        }
        if (hasta != null && !cuando.isBefore(hasta.plusDays(1).atStartOfDay())) {
            return false;
        }
        return true;
    }

    private BigDecimal sumar(List<PagoResponse> pagos, Function<PagoResponse, BigDecimal> getter) {
        return pagos.stream()
                .map(getter)
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private PagosResumen resumenPagos(List<PagoResponse> pagos) {
        PagosResumen resumen = new PagosResumen();
        for (PagoResponse p : pagos) {
            BigDecimal total = p.getTotal() != null ? p.getTotal() : BigDecimal.ZERO;
            if (p.getEstado() == EstadoPago.APROBADO) {
                resumen.aprobados++;
                resumen.montoAprobado = resumen.montoAprobado.add(total);
            } else if (p.getEstado() == EstadoPago.PENDIENTE) {
                resumen.pendientes++;
                resumen.montoPendiente = resumen.montoPendiente.add(total);
            } else if (p.getEstado() == EstadoPago.RECHAZADO) {
                resumen.rechazados++;
                resumen.montoRechazado = resumen.montoRechazado.add(total);
            } else {
                resumen.otros++;
            }
        }
        resumen.montoAprobado = redondear(resumen.montoAprobado);
        resumen.montoPendiente = redondear(resumen.montoPendiente);
        resumen.montoRechazado = redondear(resumen.montoRechazado);
        return resumen;
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal precioVenta(Producto p) {
        return p.getOfferPrice() != null ? p.getOfferPrice() : p.getPrecio();
    }

    private boolean incrementa(TipoMovimiento tipo, int cantidad) {
        if (tipo == null) {
            return false;
        }
        return switch (tipo) {
            case ENTRADA, DEVOLUCION -> true;
            case VENTA, DANO, PERDIDA -> false;
            case AJUSTE -> cantidad > 0;
        };
    }

    private String productoNombre(Integer productoId) {
        return productoRepository.findById(productoId)
                .map(Producto::getNombre).orElse("Producto #" + productoId);
    }

    private String capitalize(String texto) {
        if (texto == null || texto.isBlank()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private List<PagoResponse> filtrarPagos(String desde, String hasta, String estado, String metodo) {
        LocalDate fDesde = parseFecha(desde);
        LocalDate fHasta = parseFecha(hasta);
        return pagoService.listar().stream()
                .filter(p -> dentroDeRango(p.getCreatedAt(), fDesde, fHasta))
                .filter(p -> "todos".equals(estado) || (p.getEstado() != null && p.getEstado().getValor().equalsIgnoreCase(estado)))
                .filter(p -> "todos".equals(metodo) || (p.getMetodo() != null && p.getMetodo().getValor().equals(metodo)))
                .toList();
    }

    private List<Producto> filtrarProductos(Integer categoriaId, String estado, String q) {
        return productoRepository.findAllByOrderByIdDesc().stream()
                .filter(p -> categoriaId == null || (p.getCategoria() != null && p.getCategoria().getId().equals(categoriaId)))
                .filter(p -> switch (estado) {
                    case "activos" -> Boolean.TRUE.equals(p.getActivo());
                    case "inactivos" -> !Boolean.TRUE.equals(p.getActivo());
                    default -> true;
                })
                .filter(p -> q == null || q.isBlank() || p.getNombre().toLowerCase().contains(q.toLowerCase()))
                .toList();
    }

    private List<InventarioMovimiento> filtrarMovimientos(String desde, String hasta, String tipo) {
        LocalDate fDesde = parseFecha(desde);
        LocalDate fHasta = parseFecha(hasta);
        return movimientoRepository.findAllByOrderByIdDesc().stream()
                .filter(m -> dentroDeRango(m.getCreatedAt(), fDesde, fHasta))
                .filter(m -> "todos".equals(tipo) || (m.getTipo() != null && m.getTipo().getValor().equals(tipo)))
                .toList();
    }

    // ── EXPORTACIÓN A EXCEL (.XLSX) ───────────────────────────────────────

    @GetMapping("/pagos/excel")
    public ResponseEntity<byte[]> excelPagos(@RequestParam(name = "desde", required = false) String desde,
                                             @RequestParam(name = "hasta", required = false) String hasta,
                                             @RequestParam(name = "estado", required = false, defaultValue = "todos") String estado,
                                             @RequestParam(name = "metodo", required = false, defaultValue = "todos") String metodo) {

        List<PagoResponse> pagos = filtrarPagos(desde, hasta, estado, metodo);
        BigDecimal totalGeneral = sumar(pagos, PagoResponse::getTotal);

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("Reporte de Pagos");
            stylerHoja(hoja, 7);

            int fila = tituloEncabezado(wb, hoja, "REPORTE DE PAGOS", 7);
            if ((desde != null && !desde.isBlank()) || (hasta != null && !hasta.isBlank())) {
                Row rango = hoja.createRow(fila++);
                rango.createCell(0).setCellValue("Rango de fechas: "
                        + (desde == null || desde.isBlank() ? "inicio" : desde)
                        + " al " + (hasta == null || hasta.isBlank() ? "hoy" : hasta));
            }

            Row encabezado = hoja.createRow(fila);
            String[] columnas = {"N.º", "Pedido", "Cliente", "Fecha", "Método", "Estado", "Total"};
            for (int c = 0; c < columnas.length; c++) {
                encabezado.createCell(c).setCellValue(columnas[c]);
                encabezado.getCell(c).setCellStyle(styleBanda(wb, true));
            }

            int n = 1;
            for (PagoResponse p : pagos) {
                Row r = hoja.createRow(++fila);
                r.createCell(0).setCellValue(n++);
                r.createCell(1).setCellValue(p.getPedidoId() != null ? String.valueOf(p.getPedidoId()) : "");
                r.createCell(2).setCellValue(p.getNombre() != null ? p.getNombre() : "");
                r.createCell(3).setCellValue(p.getCreatedAt() != null
                        ? p.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
                r.createCell(4).setCellValue(p.getMetodo() != null ? capitalize(p.getMetodo().getValor()) : "");
                r.createCell(5).setCellValue(p.getEstado() != null ? capitalize(p.getEstado().getValor()) : "");
                Cell total = r.createCell(6);
                total.setCellValue(p.getTotal() != null ? p.getTotal().doubleValue() : 0d);
                total.setCellStyle(styleNumero(wb, false));
            }

            Row totalRow = hoja.createRow(++fila);
            totalRow.createCell(5).setCellValue("TOTAL GENERAL");
            Cell totalFinal = totalRow.createCell(6);
            totalFinal.setCellValue(totalGeneral.doubleValue());
            totalFinal.setCellStyle(styleNumero(wb, true));

            return respuestaExcel(wb, out, "reporte-pagos.xlsx");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/productos/excel")
    public ResponseEntity<byte[]> excelProductos(@RequestParam(name = "categoria", required = false) Integer categoriaId,
                                                 @RequestParam(name = "estado", required = false, defaultValue = "todos") String estado,
                                                 @RequestParam(name = "q", required = false) String q) {

        List<Producto> productos = filtrarProductos(categoriaId, estado, q);

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("Reporte de Productos");
            stylerHoja(hoja, 8);

            int fila = tituloEncabezado(wb, hoja, "REPORTE DE PRODUCTOS", 8);

            Row encabezado = hoja.createRow(fila);
            String[] columnas = {"ID", "Nombre", "Categoría", "Precio", "Precio oferta", "Stock", "Unid. vendidas", "Estado"};
            for (int c = 0; c < columnas.length; c++) {
                encabezado.createCell(c).setCellValue(columnas[c]);
                encabezado.getCell(c).setCellStyle(styleBanda(wb, true));
            }

            Map<Integer, Integer> vendidas = detallePedidoRepository.findAll().stream()
                    .collect(Collectors.groupingBy(DetallePedido::getProductoId,
                            Collectors.summingInt(DetallePedido::getCantidad)));

            BigDecimal valorInventario = BigDecimal.ZERO;
            for (Producto p : productos) {
                Row r = hoja.createRow(++fila);
                r.createCell(0).setCellValue(p.getId());
                r.createCell(1).setCellValue(p.getNombre() != null ? p.getNombre() : "");
                r.createCell(2).setCellValue(p.getCategoria() != null ? p.getCategoria().getNombre() : "");
                Cell precio = r.createCell(3);
                precio.setCellValue(p.getPrecio() != null ? p.getPrecio().doubleValue() : 0d);
                precio.setCellStyle(styleNumero(wb, false));
                Cell oferta = r.createCell(4);
                oferta.setCellValue(p.getOfferPrice() != null ? p.getOfferPrice().doubleValue()
                        : p.getPrecio() != null ? p.getPrecio().doubleValue() : 0d);
                oferta.setCellStyle(styleNumero(wb, false));
                r.createCell(5).setCellValue(p.getStock() != null ? p.getStock() : 0);
                r.createCell(6).setCellValue(vendidas.getOrDefault(p.getId(), 0));
                r.createCell(7).setCellValue(Boolean.TRUE.equals(p.getActivo()) ? "Activo" : "Inactivo");
                valorInventario = valorInventario.add(precioVenta(p).multiply(BigDecimal.valueOf(p.getStock() != null ? p.getStock() : 0)));
            }

            Row totalRow = hoja.createRow(++fila);
            totalRow.createCell(0).setCellValue("TOTAL PRODUCTOS: " + productos.size());
            Cell valorRow = totalRow.createCell(6);
            valorRow.setCellValue(String.format("Valor inv.: $%,.2f", valorInventario));
            valorRow.setCellStyle(styleNumero(wb, true));

            return respuestaExcel(wb, out, "reporte-productos.xlsx");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/inventario/excel")
    public ResponseEntity<byte[]> excelInventario(@RequestParam(name = "desde", required = false) String desde,
                                                  @RequestParam(name = "hasta", required = false) String hasta,
                                                  @RequestParam(name = "tipo", required = false, defaultValue = "todos") String tipo) {

        List<InventarioMovimiento> movimientos = filtrarMovimientos(desde, hasta, tipo);

        long entradas = 0;
        long salidas = 0;
        for (InventarioMovimiento m : movimientos) {
            int cantidad = m.getCantidad() != null ? m.getCantidad() : 0;
            if (incrementa(m.getTipo(), cantidad)) {
                entradas += Math.abs(cantidad);
            } else {
                salidas += Math.abs(cantidad);
            }
        }

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet hoja = wb.createSheet("Reporte de Inventario");
            stylerHoja(hoja, 6);

            int fila = tituloEncabezado(wb, hoja, "REPORTE DE INVENTARIO", 6);
            Row resumen = hoja.createRow(fila++);
            resumen.createCell(0).setCellValue("Entradas: " + entradas + "  |  Salidas: " + salidas);

            Row encabezado = hoja.createRow(fila);
            String[] columnas = {"ID", "Producto", "Tipo", "Cantidad", "Usuario", "Fecha"};
            for (int c = 0; c < columnas.length; c++) {
                encabezado.createCell(c).setCellValue(columnas[c]);
                encabezado.getCell(c).setCellStyle(styleBanda(wb, true));
            }

            for (InventarioMovimiento m : movimientos) {
                Row r = hoja.createRow(++fila);
                r.createCell(0).setCellValue(m.getId());
                r.createCell(1).setCellValue(m.getProductoNombre() != null ? m.getProductoNombre() : "");
                r.createCell(2).setCellValue(m.getTipo() != null ? capitalize(m.getTipo().getValor()) : "");
                int cantidad = m.getCantidad() != null ? m.getCantidad() : 0;
                boolean inc = incrementa(m.getTipo(), cantidad);
                r.createCell(3).setCellValue((inc ? "+" : "-") + Math.abs(cantidad));
                r.createCell(4).setCellValue(m.getUsuario() != null ? m.getUsuario() : "");
                r.createCell(5).setCellValue(m.getCreatedAt() != null
                        ? m.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
            }

            return respuestaExcel(wb, out, "reporte-inventario.xlsx");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<byte[]> respuestaExcel(Workbook wb, ByteArrayOutputStream out, String nombre) throws IOException {
        wb.write(out);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombre + "\"");
        return new ResponseEntity<>(out.toByteArray(), headers, HttpStatus.OK);
    }

    private void stylerHoja(Sheet hoja, int columnas) {
        hoja.setColumnWidth(0, 16 * 256);
        for (int c = 1; c < columnas; c++) {
            hoja.setColumnWidth(c, 22 * 256);
        }
        hoja.createFreezePane(0, 5);
    }

    /**
     * Encabezado estilo PDF: logo de la empresa, nombre comercial, NIT,
     * teléfono y correo tomados de la tabla configuración, título del
     * reporte y fecha/último usuario que lo genera.
     */
    private int tituloEncabezado(Workbook wb, Sheet hoja, String titulo, int columnas) {
        Map<String, String> emp = configuracionService.mapa();
        String nombreTienda = emp.getOrDefault("nombreTienda", "Olympic Store");

        // Fila 0: logo + razón social (barra azul marino)
        hoja.addMergedRegion(new CellRangeAddress(0, 0, 1, columnas - 1));
        Row r0 = hoja.createRow(0);
        r0.setHeightInPoints(44);
        Cell c0 = r0.createCell(1);
        c0.setCellValue(nombreTienda.toUpperCase());
        c0.setCellStyle(styleEncabezado(wb, true));
        for (int c = 1; c < columnas; c++) {
            Cell cel = r0.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            if (cel == null) {
                cel = r0.createCell(c);
            }
            cel.setCellStyle(styleEncabezado(wb, true));
        }
        insertarLogo(wb, hoja);

        // Fila 1: datos de contacto (crema)
        hoja.addMergedRegion(new CellRangeAddress(1, 1, 1, columnas - 1));
        Row r1 = hoja.createRow(1);
        r1.setHeightInPoints(24);
        StringBuilder contacto = new StringBuilder();
        String nit = emp.get("nit");
        String telefono = emp.get("telefono");
        String correo = emp.get("correo");
        String direccion = emp.get("direccion");
        if (nit != null && !nit.isBlank()) {
            contacto.append("NIT: ").append(nit);
        }
        if (telefono != null && !telefono.isBlank()) {
            aniadir(contacto, "Tel: " + telefono);
        }
        if (correo != null && !correo.isBlank()) {
            aniadir(contacto, correo);
        }
        if (direccion != null && !direccion.isBlank()) {
            aniadir(contacto, "Dir: " + direccion);
        }
        Cell c1 = r1.createCell(1);
        c1.setCellValue(contacto.toString());
        c1.setCellStyle(styleContacto(wb));

        // Fila 2: título del reporte (barra dorada)
        hoja.addMergedRegion(new CellRangeAddress(2, 2, 0, columnas - 1));
        Row r2 = hoja.createRow(2);
        r2.setHeightInPoints(26);
        Cell c2 = r2.createCell(0);
        c2.setCellValue(titulo);
        c2.setCellStyle(styleEncabezado(wb, false));

        // Fila 3: fecha de generación
        hoja.addMergedRegion(new CellRangeAddress(3, 3, 0, columnas - 1));
        Row r3 = hoja.createRow(3);
        Cell c3 = r3.createCell(0);
        c3.setCellValue("Generado el " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                + " por " + usuarioActual());
        c3.setCellStyle(styleBanda(wb, false));

        // Fila 4: encabezados de columna (la escribe cada método después de este)
        return 4;
    }

    private void aniadir(StringBuilder sb, String dato) {
        if (sb.length() > 0) {
            sb.append("   |   ");
        }
        sb.append(dato);
    }

    private byte[] cargarLogo() {
        try (InputStream in = getClass().getResourceAsStream("/static/marca/logo.png")) {
            if (in == null) {
                return null;
            }
            try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                in.transferTo(out);
                return out.toByteArray();
            }
        } catch (IOException e) {
            return null;
        }
    }

    private void insertarLogo(Workbook wb, Sheet hoja) {
        byte[] logo = cargarLogo();
        if (logo == null) {
            return;
        }
        try {
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(logo));
            if (img == null || img.getHeight() <= 0) {
                return;
            }
            double altoObjetivo = 40;
            double escala = altoObjetivo / img.getHeight();
            int pictureIdx = wb.addPicture(logo, Workbook.PICTURE_TYPE_PNG);
            Drawing<?> drawing = hoja.createDrawingPatriarch();
            CreationHelper helper = wb.getCreationHelper();
            ClientAnchor anchor = helper.createClientAnchor();
            anchor.setCol1(0);
            anchor.setRow1(0);
            anchor.setCol2(1);
            anchor.setRow2(1);
            Picture pic = drawing.createPicture(anchor, pictureIdx);
            pic.resize(escala);
        } catch (IOException e) {
            // Si el logo no puede leerse, se omite sin romper la exportación
        }
    }

    private CellStyle styleEncabezado(Workbook wb, boolean navy) {
        CellStyle style = wb.createCellStyle();
        XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
        xssfStyle.setFillForegroundColor(new XSSFColor(
                navy ? new byte[]{(byte) 0x1b, (byte) 0x2a, (byte) 0x4e}
                        : new byte[]{(byte) 0xC9, (byte) 0xA2, (byte) 0x27}, null));
        xssfStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont font = (XSSFFont) wb.createFont();
        font.setBold(true);
        if (navy) {
            font.setColor(new XSSFColor(new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF}, null));
            font.setFontHeightInPoints((short) 14);
        } else {
            font.setColor(new XSSFColor(new byte[]{(byte) 0x1b, (byte) 0x2a, (byte) 0x4e}, null));
            font.setFontHeightInPoints((short) 12);
        }
        style.setFont(font);
        return style;
    }

    private CellStyle styleContacto(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        XSSFCellStyle xssfStyle = (XSSFCellStyle) style;
        xssfStyle.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 0xF8, (byte) 0xF4, (byte) 0xEA}, null));
        xssfStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        xssfStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        XSSFFont font = (XSSFFont) wb.createFont();
        font.setColor(new XSSFColor(new byte[]{(byte) 0x1b, (byte) 0x2a, (byte) 0x4e}, null));
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        return style;
    }

    private CellStyle styleBanda(Workbook wb, boolean resaltado) {
        CellStyle style = wb.createCellStyle();
        if (resaltado) {
            style.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }

    private CellStyle styleNumero(Workbook wb, boolean total) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));
        if (total) {
            style.setFillForegroundColor(IndexedColors.GOLD.getIndex());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font bold = wb.createFont();
            bold.setBold(true);
            style.setFont(bold);
        }
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    public static class PagosResumen {
        public long aprobados;
        public long pendientes;
        public long rechazados;
        public long otros;
        public BigDecimal montoAprobado = BigDecimal.ZERO;
        public BigDecimal montoPendiente = BigDecimal.ZERO;
        public BigDecimal montoRechazado = BigDecimal.ZERO;
    }
}