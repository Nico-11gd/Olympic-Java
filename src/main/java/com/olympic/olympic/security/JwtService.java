package com.olympic.olympic.security;

import com.olympic.olympic.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Generación y validación de tokens JWT.
 * El token identifica: id, nombre, correo (subject) y rol del usuario,
 * tal como pide el módulo de autenticación.
 *
 * El JWT viaja al navegador como cookie HttpOnly (ver
 * crearCookie/crearCookieExpirada),
 * no vive en localStorage: así viaja solo en cada request (incluida la carga
 * normal
 * de una página, no solo en las llamadas fetch()) y JavaScript no puede leerlo,
 * lo cual protege contra robo del token vía XSS.
 */
@Service
public class JwtService {

    /**
     * Nombre único de la cookie del JWT, compartido con JwtAuthenticationFilter y
     * AuthController.
     */
    public static final String COOKIE_NAME = "olympic_token";

    private final SecretKey llave;
    private final long expiracionMs;
    private final boolean cookieSecure;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expiracionMs,
            @Value("${app.jwt.cookie-secure:false}") boolean cookieSecure) {
        this.llave = Keys.hmacShaKeyFor(java.util.Base64.getDecoder().decode(secret));
        this.expiracionMs = expiracionMs;
        this.cookieSecure = cookieSecure;
    }

    /** Cookie que se envía al hacer login (dura lo mismo que el JWT). */
    public ResponseCookie crearCookie(String token) {
        return ResponseCookie.from(COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure) // true en producción (HTTPS) — ver application.yml
                .sameSite("Lax")
                .path("/")
                .maxAge(expiracionMs / 1000)
                .build();
    }

    /**
     * Cookie "vacía" que reemplaza a la anterior en el navegador al hacer logout.
     */
    public ResponseCookie crearCookieExpirada() {
        return ResponseCookie.from(COOKIE_NAME, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + expiracionMs);

        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().getValor())
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(llave)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(llave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token) {
        try {
            Claims claims = extraerClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }
}
