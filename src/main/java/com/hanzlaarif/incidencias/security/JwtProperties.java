package com.hanzlaarif.incidencias.security;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Configuración JWT ({@code app.jwt.*} en application.yml).
 *
 * @param secreto           clave HMAC en Base64; debe ocupar al menos 256 bits (32 bytes) para HS256
 * @param expiracionMinutos validez del token
 */
@Validated
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(@NotBlank String secreto, @Min(1) long expiracionMinutos) {
}
