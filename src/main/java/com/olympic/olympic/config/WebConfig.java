package com.olympic.olympic.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Publica el directorio configurable de imágenes de productos (app.upload.dir)
 * bajo la URL /uploads/**, para que las imágenes subidas se puedan mostrar
 * sin reiniciar la aplicación ni empaquetarlas dentro del .jar.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDir;

    public WebConfig(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = uploadDir;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String ubicacion = "file:" + Paths.get(uploadDir).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(ubicacion);
    }

    // Evita que el navegador guarde en caché las páginas privadas (o /login):
    // así el botón "Atrás" no puede volver a mostrarlas como si la sesión
    // siguiera activa después de un logout. Solo se aplica a estas rutas
    // puntuales — el resto de la app (css/js/imágenes) se sigue cacheando
    // normalmente. Cuando exista el panel cliente, agrega "/cliente/**" aquí.
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
                return true;
            }
        }).addPathPatterns("/admin/**", "/login");
    }
}
