package com.olympic.olympic.config;

import com.olympic.olympic.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

/**
 * Seguridad basada en sesión (JSESSIONID) con formulario de login propio,
 * igual que en OdontoClinic. Ya no hay JWT, ni API REST de autenticación,
 * ni JavaScript involucrado: todo el flujo es formularios HTML -> POST ->
 * redirect, resuelto 100% por Spring Security + Thymeleaf.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/index", "/producto", "/coleccion",
                                "/login", "/registro",
                                "/recuperar-password", "/recuperar-password/**",
                                "/carrito", "/carrito/**",
                                "/imagenes/**", "/marca/**", "/video/**", "/favicon.ico",
                                "/css/**")
                        .permitAll()
                        // El dashboard (/admin) es la home por rol: cualquier usuario
                        // autenticado lo ve y se adapta a su rol. El resto del panel
                        // (/admin/**) sigue reservado al ADMIN.
                        .requestMatchers("/admin").authenticated()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("correo")
                        .passwordParameter("password")
                        .successHandler(exitoPorRol())
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler exitoPorRol() {
        return (jakarta.servlet.http.HttpServletRequest request,
                jakarta.servlet.http.HttpServletResponse response,
                Authentication authentication) -> {

            boolean esAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            // Ambos roles aterrizan en el dashboard (/admin): ADMIN ve sus métricas
            // y el menú completo; CLIENTE ve su propia vista de cuenta.
            try {
                response.sendRedirect("/admin");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
