package com.hanzlaarif.incidencias.security;

import com.hanzlaarif.incidencias.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/** Carga usuarios por email para el login y para validar cada token. */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioAutenticado loadUserByUsername(String email) {
        return usuarioRepository.findByEmail(email.toLowerCase())
                .map(UsuarioAutenticado::de)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
