package com.hanzlaarif.incidencias.service;

import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Incidencia;
import com.hanzlaarif.incidencias.domain.Usuario;
import com.hanzlaarif.incidencias.exception.ConflictoException;
import com.hanzlaarif.incidencias.exception.RecursoNoEncontradoException;
import com.hanzlaarif.incidencias.exception.SolicitudNoValidaException;
import com.hanzlaarif.incidencias.repository.IncidenciaRepository;
import com.hanzlaarif.incidencias.repository.UsuarioRepository;
import com.hanzlaarif.incidencias.security.UsuarioAutenticado;
import com.hanzlaarif.incidencias.web.dto.ActualizarIncidenciaRequest;
import com.hanzlaarif.incidencias.web.dto.CrearIncidenciaRequest;
import com.hanzlaarif.incidencias.web.dto.IncidenciaResponse;
import com.hanzlaarif.incidencias.web.dto.PaginaResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.asignadaA;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.conEstado;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.conPrioridad;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.contieneTexto;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.creadaPor;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.todas;

/**
 * Lógica de negocio de las incidencias.
 * <p>
 * Reglas de acceso: un USUARIO solo puede ver, editar y comentar sus propias incidencias;
 * un TECNICO puede con todas. Las operaciones exclusivas de técnicos (cambiar estado,
 * asignar, eliminar) se restringen además en el controlador con {@code @PreAuthorize}.
 */
@Service
@Transactional
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final UsuarioRepository usuarioRepository;

    public IncidenciaService(IncidenciaRepository incidenciaRepository, UsuarioRepository usuarioRepository) {
        this.incidenciaRepository = incidenciaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public IncidenciaResponse crear(CrearIncidenciaRequest request, UsuarioAutenticado actual) {
        Usuario creador = usuarioRepository.findById(actual.id())
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario", actual.id()));
        Incidencia incidencia = new Incidencia(request.titulo().trim(), request.descripcion().trim(),
                request.prioridad(), creador);
        return IncidenciaResponse.de(incidenciaRepository.save(incidencia));
    }

    @Transactional(readOnly = true)
    public PaginaResponse<IncidenciaResponse> listar(FiltroIncidencias filtro, Pageable pageable,
                                                     UsuarioAutenticado actual) {
        // un USUARIO solo ve las suyas, independientemente de los filtros que envíe
        Long soloDelUsuario = actual.esTecnico() ? null : actual.id();
        var especificacion = todas(
                creadaPor(soloDelUsuario),
                conEstado(filtro.estado()),
                conPrioridad(filtro.prioridad()),
                asignadaA(filtro.tecnicoId()),
                contieneTexto(filtro.texto()));
        return PaginaResponse.de(incidenciaRepository.findAll(especificacion, pageable), IncidenciaResponse::de);
    }

    @Transactional(readOnly = true)
    public IncidenciaResponse obtener(Long id, UsuarioAutenticado actual) {
        return IncidenciaResponse.de(cargarConAcceso(id, actual));
    }

    public IncidenciaResponse actualizar(Long id, ActualizarIncidenciaRequest request, UsuarioAutenticado actual) {
        Incidencia incidencia = cargarConAcceso(id, actual);
        if (!actual.esTecnico() && incidencia.getEstado() != EstadoIncidencia.ABIERTA) {
            throw new ConflictoException("Solo puedes modificar tus incidencias mientras están ABIERTAS");
        }
        incidencia.actualizarDatos(request.titulo().trim(), request.descripcion().trim(), request.prioridad());
        return guardar(incidencia);
    }

    public IncidenciaResponse cambiarEstado(Long id, EstadoIncidencia nuevoEstado) {
        Incidencia incidencia = cargar(id);
        EstadoIncidencia actual = incidencia.getEstado();
        if (!actual.puedePasarA(nuevoEstado)) {
            throw new ConflictoException("No se puede pasar una incidencia de " + actual + " a " + nuevoEstado
                    + ". Transiciones permitidas desde " + actual + ": " + actual.siguientesPermitidos());
        }
        incidencia.cambiarEstado(nuevoEstado);
        return guardar(incidencia);
    }

    public IncidenciaResponse asignarTecnico(Long id, Long tecnicoId) {
        Incidencia incidencia = cargar(id);
        if (incidencia.getEstado() == EstadoIncidencia.CERRADA) {
            throw new ConflictoException("No se puede asignar una incidencia CERRADA");
        }
        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> new SolicitudNoValidaException("No existe ningún usuario con id " + tecnicoId));
        if (!tecnico.esTecnico()) {
            throw new SolicitudNoValidaException("El usuario " + tecnicoId + " no tiene rol TECNICO");
        }
        incidencia.asignarTecnico(tecnico);
        return guardar(incidencia);
    }

    public void eliminar(Long id) {
        incidenciaRepository.delete(cargar(id));
    }

    /** Fuerza el flush para que @PreUpdate actualice fechaActualizacion antes de construir la respuesta. */
    private IncidenciaResponse guardar(Incidencia incidencia) {
        return IncidenciaResponse.de(incidenciaRepository.saveAndFlush(incidencia));
    }

    /**
     * Carga una incidencia comprobando que el usuario puede acceder a ella.
     * Lo reutiliza también el servicio de comentarios.
     */
    Incidencia cargarConAcceso(Long id, UsuarioAutenticado actual) {
        Incidencia incidencia = cargar(id);
        if (!actual.esTecnico() && !incidencia.perteneceA(actual.id())) {
            throw new AccessDeniedException("La incidencia " + id + " no te pertenece");
        }
        return incidencia;
    }

    private Incidencia cargar(Long id) {
        return incidenciaRepository.findWithUsuariosById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Incidencia", id));
    }
}
