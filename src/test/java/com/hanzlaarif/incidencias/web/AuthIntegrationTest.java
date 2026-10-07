package com.hanzlaarif.incidencias.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestBase {

    @Test
    void registroDevuelve201ConRolUsuarioYSinPassword() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Ana", "email", "ana@test.local", "password", PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.rol").value("USUARIO"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void registroConEmailRepetidoDevuelve409() throws Exception {
        registrar("ana@test.local");

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Otra Ana", "email", "ANA@test.local", "password", PASSWORD))))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void registroConDatosInvalidosDevuelve400ConErroresPorCampo() throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "", "email", "no-es-un-email", "password", "corta"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errores.nombre").exists())
                .andExpect(jsonPath("$.errores.email").exists())
                .andExpect(jsonPath("$.errores.password").exists());
    }

    @Test
    void loginCorrectoDevuelveTokenQuePermiteAcceder() throws Exception {
        String token = registrarYLogin("ana@test.local");

        mockMvc.perform(get("/api/usuarios/me").header(HttpHeaders.AUTHORIZATION, token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("ana@test.local"));
    }

    @Test
    void loginConPasswordIncorrectaDevuelve401() throws Exception {
        registrar("ana@test.local");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "ana@test.local", "password", "otra-contrasena"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos"));
    }

    @Test
    void loginConEmailInexistenteDevuelveElMismo401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", "nadie@test.local", "password", PASSWORD))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").value("Email o contraseña incorrectos"));
    }

    @Test
    void sinTokenDevuelve401() throws Exception {
        mockMvc.perform(get("/api/incidencias"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void conTokenInvalidoDevuelve401() throws Exception {
        mockMvc.perform(get("/api/incidencias").header(HttpHeaders.AUTHORIZATION, "Bearer esto.no.vale"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void laDocumentacionOpenApiEsPublica() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("API de gestión de incidencias"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"));
    }
}
