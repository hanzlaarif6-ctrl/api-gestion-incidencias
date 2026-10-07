package com.hanzlaarif.incidencias.security;

import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.domain.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuario autenticado tal y como lo ve Spring Security.
 * Es un objeto propio (y no la entidad JPA) para no exponer la entidad fuera de la
 * capa de persistencia ni arrastrar relaciones lazy al contexto de seguridad.
 */
public record UsuarioAutenticado(Long id, String email, String password, Rol rol) implements UserDetails {

    public static UsuarioAutenticado de(Usuario usuario) {
        return new UsuarioAutenticado(usuario.getId(), usuario.getEmail(), usuario.getPassword(), usuario.getRol());
    }

    public boolean esTecnico() {
        return rol == Rol.TECNICO;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // hasRole('TECNICO') comprueba la autoridad "ROLE_TECNICO"
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }
}
