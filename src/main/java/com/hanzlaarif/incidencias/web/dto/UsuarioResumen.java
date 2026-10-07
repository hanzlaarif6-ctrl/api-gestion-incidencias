package com.hanzlaarif.incidencias.web.dto;

import com.hanzlaarif.incidencias.domain.Usuario;

/** Datos mínimos de un usuario para incrustar en otras respuestas. */
public record UsuarioResumen(Long id, String nombre, String email) {

    public static UsuarioResumen de(Usuario usuario) {
        return usuario == null ? null : new UsuarioResumen(usuario.getId(), usuario.getNombre(), usuario.getEmail());
    }
}
