package com.hanzlaarif.incidencias.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @Schema(example = "ana.garcia@ejemplo.com") @NotBlank String email,
        @Schema(example = "contrasena-segura") @NotBlank String password) {
}
