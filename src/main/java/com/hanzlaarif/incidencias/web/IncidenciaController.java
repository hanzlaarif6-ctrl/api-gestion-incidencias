package com.hanzlaarif.incidencias.web;

import com.hanzlaarif.incidencias.config.OpenApiConfig;
import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Prioridad;
import com.hanzlaarif.incidencias.exception.SolicitudNoValidaException;
import com.hanzlaarif.incidencias.security.UsuarioAutenticado;
import com.hanzlaarif.incidencias.service.FiltroIncidencias;
import com.hanzlaarif.incidencias.service.IncidenciaService;
import com.hanzlaarif.incidencias.web.dto.ActualizarIncidenciaRequest;
import com.hanzlaarif.incidencias.web.dto.AsignacionRequest;
import com.hanzlaarif.incidencias.web.dto.CambioEstadoRequest;
import com.hanzlaarif.incidencias.web.dto.CrearIncidenciaRequest;
import com.hanzlaarif.incidencias.web.dto.IncidenciaResponse;
import com.hanzlaarif.incidencias.web.dto.PaginaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.Set;

@RestController
@RequestMapping("/api/incidencias")
@Tag(name = "Incidencias")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class IncidenciaController {

    /** Campos por los que se permite ordenar; cualquier otro devuelve 400 en lugar de un error interno. */
    private static final Set<String> CAMPOS_ORDENABLES = Set.of("id", "titulo", "fechaCreacion", "fechaActualizacion");

    private final IncidenciaService incidenciaService;

    public IncidenciaController(IncidenciaService incidenciaService) {
        this.incidenciaService = incidenciaService;
    }

    @GetMapping
    @Operation(summary = "Lista incidencias con filtros y paginación",
            description = "Un USUARIO solo recibe las suyas; un TECNICO, todas. "
                    + "Ordenable por: id, titulo, fechaCreacion, fechaActualizacion (p. ej. sort=fechaCreacion,desc).")
    public PaginaResponse<IncidenciaResponse> listar(
            @RequestParam(required = false) EstadoIncidencia estado,
            @RequestParam(required = false) Prioridad prioridad,
            @RequestParam(required = false) Long tecnicoId,
            @RequestParam(required = false) String texto,
            @ParameterObject @PageableDefault(size = 20, sort = "fechaCreacion", direction = Sort.Direction.DESC)
            Pageable pageable,
            @AuthenticationPrincipal UsuarioAutenticado actual) {
        validarOrdenacion(pageable.getSort());
        var filtro = new FiltroIncidencias(estado, prioridad, tecnicoId, texto);
        return incidenciaService.listar(filtro, pageable, actual);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una incidencia (propia, o cualquiera si eres técnico)")
    public IncidenciaResponse obtener(@PathVariable Long id, @AuthenticationPrincipal UsuarioAutenticado actual) {
        return incidenciaService.obtener(id, actual);
    }

    @PostMapping
    @Operation(summary = "Crea una incidencia en estado ABIERTA")
    public ResponseEntity<IncidenciaResponse> crear(@Valid @RequestBody CrearIncidenciaRequest request,
                                                    @AuthenticationPrincipal UsuarioAutenticado actual) {
        IncidenciaResponse creada = incidenciaService.crear(request, actual);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creada.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifica título, descripción y prioridad",
            description = "Un USUARIO solo puede modificar sus incidencias mientras están ABIERTAS.")
    public IncidenciaResponse actualizar(@PathVariable Long id,
                                         @Valid @RequestBody ActualizarIncidenciaRequest request,
                                         @AuthenticationPrincipal UsuarioAutenticado actual) {
        return incidenciaService.actualizar(id, request, actual);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('TECNICO')")
    @Operation(summary = "Cambia el estado (solo TECNICO)",
            description = "ABIERTA→EN_CURSO|CERRADA · EN_CURSO→RESUELTA|ABIERTA · RESUELTA→CERRADA|EN_CURSO")
    public IncidenciaResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest request) {
        return incidenciaService.cambiarEstado(id, request.estado());
    }

    @PatchMapping("/{id}/tecnico")
    @PreAuthorize("hasRole('TECNICO')")
    @Operation(summary = "Asigna la incidencia a un técnico (solo TECNICO)")
    public IncidenciaResponse asignar(@PathVariable Long id, @Valid @RequestBody AsignacionRequest request) {
        return incidenciaService.asignarTecnico(id, request.tecnicoId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TECNICO')")
    @Operation(summary = "Elimina una incidencia y sus comentarios (solo TECNICO)")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        incidenciaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    private static void validarOrdenacion(Sort sort) {
        for (Sort.Order orden : sort) {
            if (!CAMPOS_ORDENABLES.contains(orden.getProperty())) {
                throw new SolicitudNoValidaException("No se puede ordenar por '" + orden.getProperty()
                        + "'. Campos permitidos: " + CAMPOS_ORDENABLES);
            }
        }
    }
}
