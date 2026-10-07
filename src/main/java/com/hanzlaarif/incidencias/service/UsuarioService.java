package com.hanzlaarif.incidencias.service;

import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.exception.RecursoNoEncontradoException;
import com.hanzlaarif.incidencias.repository.UsuarioRepository;
import com.hanzlaarif.incidencias.security.UsuarioAutenticado;
import com.hanzlaarif.incidencias.web.dto.UsuarioResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioResponse perfil(UsuarioAutenticado actual) {
        return usuarioRepository.findById(actual.id())
                .map(UsuarioResponse::de)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", actual.id()));
    }

    public List<UsuarioResponse> listarTecnicos() {
        return usuarioRepository.findByRolOrderByNombreAsc(Rol.TECNICO).stream()
                .map(UsuarioResponse::de)
                .toList();
    }
}
