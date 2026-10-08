package com.hanzlaarif.incidencias.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearComentarioRequest(
        @Schema(example = "He reiniciado la impresora y sigue igual.")
        @NotBlank @Size(max = 1000) String texto) {
}
