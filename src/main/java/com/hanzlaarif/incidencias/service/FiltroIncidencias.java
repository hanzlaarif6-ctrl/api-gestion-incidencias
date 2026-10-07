package com.hanzlaarif.incidencias.service;

import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Prioridad;

/** Filtros opcionales del listado; cualquier campo puede ser {@code null}. */
public record FiltroIncidencias(EstadoIncidencia estado, Prioridad prioridad, Long tecnicoId, String texto) {

    public static FiltroIncidencias vacio() {
        return new FiltroIncidencias(null, null, null, null);
    }
}
