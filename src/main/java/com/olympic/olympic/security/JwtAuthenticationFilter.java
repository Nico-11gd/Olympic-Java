package com.olympic.olympic.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.olympic.olympic.dto.ApiResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que se ejecuta en cada petición: si viene un header
 * "Authorization: Bearer <token>" válido, autentica al usuario en el
 * SecurityContext usando la información contenida en el propio JWT
 * (id, correo, rol) sin necesidad de volver a consultar la base de datos.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = extraerToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Claims claims = jwtService.extraerClaims(token);
            String correo = claims.getSubject();
            String rol = claims.get("rol", String.class);

            if (correo != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase()));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(correo,
                        null, authorities);
                authentication.setDetails(claims);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtException | IllegalArgumentException e) {
            SecurityContextHolder.clearContext();
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(ApiResponse.error("Token inválido o expirado.")));
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Busca el JWT primero en la cookie HttpOnly (navegación normal / fetch del
     * propio frontend), y como respaldo en el header Authorization (útil para
     * probar la API con Postman/curl sin necesidad de cookie).
     */
    private String extraerToken(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (jakarta.servlet.http.Cookie cookie : request.getCookies()) {
                if (com.olympic.olympic.security.JwtService.COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIJO)) {
            return header.substring(PREFIJO.length());
        }
        return null;
    }
}
