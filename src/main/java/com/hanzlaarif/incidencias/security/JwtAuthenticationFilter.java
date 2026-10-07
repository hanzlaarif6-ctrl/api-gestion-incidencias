package com.hanzlaarif.incidencias.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lee la cabecera {@code Authorization: Bearer <token>} y, si el token es válido,
 * deja al usuario autenticado en el {@code SecurityContext} para esta petición.
 * <p>
 * Si no hay token o no es válido, no corta la petición: simplemente no autentica,
 * y es Spring Security quien responde 401 si el endpoint lo requiere.
 * <p>
 * No se declara como {@code @Component} a propósito: Spring Boot registraría
 * cualquier bean {@code Filter} también en la cadena de filtros del servidor, y se
 * ejecutaría fuera de la cadena de seguridad. Se crea en {@code SecurityConfig}.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UsuarioDetailsService usuarioDetailsService) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera != null && cabecera.startsWith(PREFIJO)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String email = jwtService.extraerEmail(cabecera.substring(PREFIJO.length()));
                UsuarioAutenticado usuario = usuarioDetailsService.loadUserByUsername(email);

                var autenticacion = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (JwtException | IllegalArgumentException | UsernameNotFoundException e) {
                // token no válido o usuario eliminado: la petición sigue sin autenticar
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }
}
