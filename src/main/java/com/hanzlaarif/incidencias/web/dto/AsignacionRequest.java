package com.hanzlaarif.incidencias.web.dto;

import jakarta.validation.constraints.NotNull;

public record AsignacionRequest(@NotNull Long tecnicoId) {
}
