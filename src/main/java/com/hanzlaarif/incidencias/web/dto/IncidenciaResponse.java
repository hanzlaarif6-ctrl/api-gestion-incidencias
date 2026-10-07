package com.hanzlaarif.incidencias.web.dto;

import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Incidencia;
import com.hanzlaarif.incidencias.domain.Prioridad;

import java.time.LocalDateTime;

public record IncidenciaResponse(
        Long id,
        String titulo,
        String descripcion,
        EstadoIncidencia estado,
        Prioridad prioridad,
        UsuarioResumen creador,
        UsuarioResumen tecnico,
        LocalDateTime fechaCreacion,
        LocalDateTime fechaActualizacion,
        LocalDateTime fechaResolucion) {

    public static IncidenciaResponse de(Incidencia incidencia) {
        return new IncidenciaResponse(
                incidencia.getId(),
                incidencia.getTitulo(),
                incidencia.getDescripcion(),
                incidencia.getEstado(),
                incidencia.getPrioridad(),
                UsuarioResumen.de(incidencia.getCreador()),
                UsuarioResumen.de(incidencia.getTecnico()),
                incidencia.getFechaCreacion(),
                incidencia.getFechaActualizacion(),
                incidencia.getFechaResolucion());
    }
}
