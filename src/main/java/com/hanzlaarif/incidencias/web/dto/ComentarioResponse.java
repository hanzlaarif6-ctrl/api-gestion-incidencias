package com.hanzlaarif.incidencias.web.dto;

import com.hanzlaarif.incidencias.domain.Comentario;

import java.time.LocalDateTime;

public record ComentarioResponse(Long id, String texto, UsuarioResumen autor, LocalDateTime fecha) {

    public static ComentarioResponse de(Comentario comentario) {
        return new ComentarioResponse(comentario.getId(), comentario.getTexto(),
                UsuarioResumen.de(comentario.getAutor()), comentario.getFecha());
    }
}
