package com.hanzlaarif.incidencias.web.dto;

import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambioEstadoRequest(@Schema(example = "EN_CURSO") @NotNull EstadoIncidencia estado) {
}
