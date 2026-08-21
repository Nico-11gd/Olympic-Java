package com.olympic.olympic.config;

import com.olympic.olympic.security.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.olympic.olympic.dto.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de Spring Security.
 *
 * Modelo de autenticación: el JWT viaja como cookie HttpOnly (ver JwtService),
 * así que llega solo en TODA petición al servidor — navegación normal
 * (escribir la URL, clic en un link, botón "Atrás") y llamadas fetch() por
 * igual. Por eso /admin/** exige hasRole("ADMIN") aquí mismo: la protección
 * de la página ya no depende únicamente de JavaScript.
 *
 * Sigue siendo JWT stateless (no hay sesión guardada en el servidor): la
 * cookie es solo el "transporte" del mismo token; JwtAuthenticationFilter lo
 * valida en cada request igual que antes.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private boolean esRutaApi(jakarta.servlet.http.HttpServletRequest request) {
        return request.getRequestURI().startsWith("/api/");
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        ObjectMapper mapper = new ObjectMapper();
        return (request, response, authException) -> {
            if (esRutaApi(request)) {
                response.setStatus(401);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        mapper.writeValueAsString(
                                ApiResponse.error("Debes iniciar sesión para acceder a este recurso.")));
            } else {
                response.sendRedirect("/login");
            }
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        ObjectMapper mapper = new ObjectMapper();
        return (request, response, accessDeniedException) -> {
            if (esRutaApi(request)) {
                response.setStatus(403);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                        mapper.writeValueAsString(ApiResponse.error("No tienes permiso para realizar esta acción.")));
            } else {
                response.sendRedirect("/login");
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .authorizeHttpRequests(auth -> auth
                        // ── Rutas públicas ──
                        .requestMatchers(
                                "/api/auth/**",
                                "/login", "/registro", "/recuperar-password",
                                "/", "/index", "/producto",
                                "/css/**", "/js/**", "/imagenes/**", "/uploads/**",
                                "/favicon.ico")
                        .permitAll()
                        // ── Catálogo y categorías: lectura pública (tienda tipo Amazon;
                        // el login solo se exige al comprar) ──
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/productos/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/categorias/**").permitAll()
                        // ── Gestión de usuarios: solo ADMIN ──
                        .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                        // ── Escritura de productos: solo ADMIN ──
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/productos/**").hasRole("ADMIN")
                        .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/productos/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/imagenes/**").hasRole("ADMIN")
                        // ── Panel de administración (páginas Thymeleaf): solo ADMIN ──
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // ── Todo lo demás requiere estar autenticado ──
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "Accept", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}