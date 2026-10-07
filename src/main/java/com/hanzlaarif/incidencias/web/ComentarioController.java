package com.hanzlaarif.incidencias.web;

import com.hanzlaarif.incidencias.config.OpenApiConfig;
import com.hanzlaarif.incidencias.security.UsuarioAutenticado;
import com.hanzlaarif.incidencias.service.ComentarioService;
import com.hanzlaarif.incidencias.web.dto.ComentarioResponse;
import com.hanzlaarif.incidencias.web.dto.CrearComentarioRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/incidencias/{incidenciaId}/comentarios")
@Tag(name = "Comentarios")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class ComentarioController {

    private final ComentarioService comentarioService;

    public ComentarioController(ComentarioService comentarioService) {
        this.comentarioService = comentarioService;
    }

    @GetMapping
    @Operation(summary = "Lista los comentarios de una incidencia, del más antiguo al más reciente")
    public List<ComentarioResponse> listar(@PathVariable Long incidenciaId,
                                           @AuthenticationPrincipal UsuarioAutenticado actual) {
        return comentarioService.listar(incidenciaId, actual);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Añade un comentario (el creador de la incidencia o un técnico)")
    public ComentarioResponse crear(@PathVariable Long incidenciaId,
                                    @Valid @RequestBody CrearComentarioRequest request,
                                    @AuthenticationPrincipal UsuarioAutenticado actual) {
        return comentarioService.crear(incidenciaId, request, actual);
    }
}
