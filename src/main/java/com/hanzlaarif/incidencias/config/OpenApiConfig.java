package com.hanzlaarif.incidencias.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación OpenAPI. Swagger UI en {@code /swagger-ui.html}.
 * El botón "Authorize" permite pegar el token devuelto por {@code /api/auth/login}.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "API de gestión de incidencias",
        version = "1.0.0",
        description = "Gestión de incidencias (tickets) de soporte técnico. "
                + "Los usuarios crean y consultan sus incidencias; los técnicos las gestionan todas."))
@SecurityScheme(name = OpenApiConfig.BEARER, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    public static final String BEARER = "bearerAuth";
}
