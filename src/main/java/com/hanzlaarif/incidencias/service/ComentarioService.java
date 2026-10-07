package com.hanzlaarif.incidencias.service;

import com.hanzlaarif.incidencias.domain.Comentario;
import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Incidencia;
import com.hanzlaarif.incidencias.domain.Usuario;
import com.hanzlaarif.incidencias.exception.ConflictoException;
import com.hanzlaarif.incidencias.exception.RecursoNoEncontradoException;
import com.hanzlaarif.incidencias.repository.ComentarioRepository;
import com.hanzlaarif.incidencias.repository.UsuarioRepository;
import com.hanzlaarif.incidencias.security.UsuarioAutenticado;
import com.hanzlaarif.incidencias.web.dto.ComentarioResponse;
import com.hanzlaarif.incidencias.web.dto.CrearComentarioRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final IncidenciaService incidenciaService;

    public ComentarioService(ComentarioRepository comentarioRepository, UsuarioRepository usuarioRepository,
                             IncidenciaService incidenciaService) {
        this.comentarioRepository = comentarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.incidenciaService = incidenciaService;
    }

    @Transactional(readOnly = true)
    public List<ComentarioResponse> listar(Long incidenciaId, UsuarioAutenticado actual) {
        incidenciaService.cargarConAcceso(incidenciaId, actual);
        return comentarioRepository.findByIncidenciaIdOrderByFechaAscIdAsc(incidenciaId).stream()
                .map(ComentarioResponse::de)
                .toList();
    }

    public ComentarioResponse crear(Long incidenciaId, CrearComentarioRequest request, UsuarioAutenticado actual) {
        Incidencia incidencia = incidenciaService.cargarConAcceso(incidenciaId, actual);
        if (incidencia.getEstado() == EstadoIncidencia.CERRADA) {
            throw new ConflictoException("No se puede comentar una incidencia CERRADA");
        }
        Usuario autor = usuarioRepository.findById(actual.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", actual.id()));
        Comentario comentario = comentarioRepository.save(new Comentario(request.texto().trim(), incidencia, autor));
        return ComentarioResponse.de(comentario);
    }
}
