package com.hanzlaarif.incidencias.web.dto;

import com.hanzlaarif.incidencias.domain.Prioridad;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Sustitución completa (PUT) de los datos editables de una incidencia. */
public record ActualizarIncidenciaRequest(
        @NotBlank @Size(max = 150) String titulo,
        @NotBlank @Size(max = 1000) String descripcion,
        @NotNull Prioridad prioridad) {
}
