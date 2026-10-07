package com.hanzlaarif.incidencias.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;

/**
 * Respuestas 401/403 que genera Spring Security antes de llegar a los controladores
 * (sin token, token no válido, rol insuficiente en reglas de URL), con el mismo
 * formato {@code application/problem+json} que el resto de errores de la API.
 */
@Component
public class ProblemDetailSecurityHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ProblemDetailSecurityHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        escribir(request, response, HttpStatus.UNAUTHORIZED,
                "Se requiere autenticación: envía un token válido en la cabecera Authorization");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(request, response, HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta operación");
    }

    private void escribir(HttpServletRequest request, HttpServletResponse response,
                          HttpStatus estado, String detalle) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(estado, detalle);
        problema.setInstance(URI.create(request.getRequestURI()));
        response.setStatus(estado.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getOutputStream(), problema);
    }
}
