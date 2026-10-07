package com.hanzlaarif.incidencias.web.dto;

import com.hanzlaarif.incidencias.domain.Prioridad;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearIncidenciaRequest(
        @Schema(example = "No funciona la impresora de la segunda planta")
        @NotBlank @Size(max = 150) String titulo,

        @Schema(example = "Al imprimir aparece el error de papel atascado aunque la bandeja está vacía.")
        @NotBlank @Size(max = 4000) String descripcion,

        @Schema(example = "MEDIA")
        @NotNull Prioridad prioridad) {
}
