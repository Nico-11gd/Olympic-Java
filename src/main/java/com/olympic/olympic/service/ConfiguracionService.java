package com.olympic.olympic.service;

import com.olympic.olympic.repository.ConfiguracionRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Acceso a los datos de configuración de la empresa (tabla `configuracion`),
 * necesarios para el encabezado de los reportes del panel admin.
 */
@Service
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public ConfiguracionService(ConfiguracionRepository configuracionRepository) {
        this.configuracionRepository = configuracionRepository;
    }

    public String obtener(String clave, String valorPorDefecto) {
        return configuracionRepository.findByClave(clave)
                .map(c -> {
                    String valor = c.getValor();
                    return (valor == null || valor.isBlank()) ? valorPorDefecto : valor;
                })
                .orElse(valorPorDefecto);
    }

    public Map<String, String> mapa() {
        Map<String, String> datos = new LinkedHashMap<>();
        datos.put("nombreTienda", obtener("nombre_tienda", "OLYMPIC Store"));
        datos.put("nit", obtener("nit", "901.001.234-5"));
        datos.put("telefono", obtener("telefono_contacto", "3001234567"));
        datos.put("correo", obtener("correo_contacto", "info@olympic.com"));
        datos.put("direccion", obtener("direccion_tienda", "Calle Principal #123"));
        return datos;
    }
}