package com.hanzlaarif.incidencias.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistroRequest(
        @Schema(example = "Ana García")
        @NotBlank @Size(max = 100) String nombre,

        @Schema(example = "ana.garcia@ejemplo.com")
        @NotBlank @Email @Size(max = 150) String email,

        // BCrypt solo tiene en cuenta los primeros 72 bytes
        @Schema(example = "contrasena-segura")
        @NotBlank @Size(min = 8, max = 72) String password) {
}
