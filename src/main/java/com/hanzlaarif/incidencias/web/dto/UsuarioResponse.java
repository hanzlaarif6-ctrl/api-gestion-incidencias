package com.hanzlaarif.incidencias.web.dto;

import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.domain.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(Long id, String nombre, String email, Rol rol, LocalDateTime fechaAlta) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getRol(), usuario.getFechaAlta());
    }
}
