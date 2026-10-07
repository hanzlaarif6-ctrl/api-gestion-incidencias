package com.hanzlaarif.incidencias.web;

import com.hanzlaarif.incidencias.config.OpenApiConfig;
import com.hanzlaarif.incidencias.security.UsuarioAutenticado;
import com.hanzlaarif.incidencias.service.UsuarioService;
import com.hanzlaarif.incidencias.web.dto.UsuarioResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios")
@SecurityRequirement(name = OpenApiConfig.BEARER)
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario autenticado")
    public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.perfil(actual);
    }

    @GetMapping("/tecnicos")
    @PreAuthorize("hasRole('TECNICO')")
    @Operation(summary = "Lista los técnicos, para asignar incidencias (solo TECNICO)")
    public List<UsuarioResponse> tecnicos() {
        return usuarioService.listarTecnicos();
    }
}
