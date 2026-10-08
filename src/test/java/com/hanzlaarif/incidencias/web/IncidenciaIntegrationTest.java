package com.hanzlaarif.incidencias.web;

import com.hanzlaarif.incidencias.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Map;

import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IncidenciaIntegrationTest extends IntegrationTestBase {

    private String tokenAna;
    private String tokenPere;
    private String tokenTecnico;
    private Usuario tecnico;

    @BeforeEach
    void prepararUsuarios() throws Exception {
        tokenAna = registrarYLogin("ana@test.local");
        tokenPere = registrarYLogin("pere@test.local");
        tecnico = crearTecnico("tecnico@test.local");
        tokenTecnico = login("tecnico@test.local");
    }

    @Test
    void crearIncidenciaDevuelve201ConLocationYEstadoAbierta() throws Exception {
        mockMvc.perform(post("/api/incidencias")
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "Sin red", "descripcion", "No hay conexión", "prioridad", "ALTA"))))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.LOCATION, matchesPattern(".*/api/incidencias/\\d+")))
                .andExpect(jsonPath("$.estado").value("ABIERTA"))
                .andExpect(jsonPath("$.creador.email").value("ana@test.local"));
    }

    @Test
    void crearConPrioridadDesconocidaDevuelve400() throws Exception {
        mockMvc.perform(post("/api/incidencias")
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "Sin red", "descripcion", "No hay conexión", "prioridad", "URGENTISIMA"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("URGENTISIMA")))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("CRITICA")));
    }

    /**
     * 1000 caracteres de 2 y 3 bytes en UTF-8 (unos 2500 bytes). En Oracle un VARCHAR2 admite como
     * máximo 4000 bytes: este test garantiza que el límite de la API cabe en cualquier base de datos.
     */
    @Test
    void descripcionMaximaConCaracteresMultibyteSeGuardaEntera() throws Exception {
        String descripcion = "ñ€".repeat(500);
        String respuesta = mockMvc.perform(post("/api/incidencias")
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "Año: caracteres àéñç", "descripcion", descripcion,
                                "prioridad", "MEDIA"))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long id = leer(respuesta).get("id").asLong();

        mockMvc.perform(get("/api/incidencias/{id}", id).header(HttpHeaders.AUTHORIZATION, tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Año: caracteres àéñç"))
                .andExpect(jsonPath("$.descripcion").value(descripcion));
    }

    @Test
    void descripcionDeMasDe1000CaracteresDevuelve400() throws Exception {
        mockMvc.perform(post("/api/incidencias")
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", "Larga", "descripcion", "a".repeat(1001), "prioridad", "BAJA"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.descripcion").exists());
    }

    @Test
    void comentarioMaximoConCaracteresMultibyteSeGuarda() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");

        comentar(tokenAna, id, "ñ€".repeat(500)).andExpect(status().isCreated());
        comentar(tokenAna, id, "a".repeat(1001)).andExpect(status().isBadRequest());
    }

    @Test
    void crearSinTituloDevuelve400() throws Exception {
        mockMvc.perform(post("/api/incidencias")
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("descripcion", "No hay conexión", "prioridad", "ALTA"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.titulo").exists());
    }

    @Test
    void unUsuarioSoloVeSusIncidenciasYElTecnicoLasVeTodas() throws Exception {
        crearIncidencia(tokenAna, "Incidencia de Ana 1", "MEDIA");
        crearIncidencia(tokenAna, "Incidencia de Ana 2", "BAJA");
        crearIncidencia(tokenPere, "Incidencia de Pere", "ALTA");

        mockMvc.perform(get("/api/incidencias").header(HttpHeaders.AUTHORIZATION, tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(2))
                .andExpect(jsonPath("$.contenido[*].creador.email", everyItem(is("ana@test.local"))));

        mockMvc.perform(get("/api/incidencias").header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3));
    }

    @Test
    void listadoFiltraYPagina() throws Exception {
        crearIncidencia(tokenAna, "Impresora 1", "ALTA");
        crearIncidencia(tokenAna, "Impresora 2", "ALTA");
        crearIncidencia(tokenAna, "Impresora 3", "ALTA");
        crearIncidencia(tokenAna, "Monitor", "BAJA");

        mockMvc.perform(get("/api/incidencias")
                        .param("prioridad", "ALTA")
                        .param("texto", "impresora")
                        .param("size", "2")
                        .param("sort", "titulo,asc")
                        .header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3))
                .andExpect(jsonPath("$.totalPaginas").value(2))
                .andExpect(jsonPath("$.tamano").value(2))
                .andExpect(jsonPath("$.contenido", hasSize(2)))
                .andExpect(jsonPath("$.contenido[0].titulo").value("Impresora 1"));
    }

    @Test
    void ordenarPorUnCampoNoPermitidoDevuelve400() throws Exception {
        mockMvc.perform(get("/api/incidencias").param("sort", "password")
                        .header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isBadRequest());
    }

    @Test
    void filtrarPorUnEstadoInexistenteDevuelve400() throws Exception {
        mockMvc.perform(get("/api/incidencias").param("estado", "PERDIDA")
                        .header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unUsuarioNoPuedeVerLaIncidenciaDeOtroUsuario() throws Exception {
        long id = crearIncidencia(tokenAna, "Privada", "MEDIA");

        mockMvc.perform(get("/api/incidencias/{id}", id).header(HttpHeaders.AUTHORIZATION, tokenPere))
                .andExpect(status().isForbidden());
    }

    @Test
    void obtenerUnaIncidenciaInexistenteDevuelve404() throws Exception {
        mockMvc.perform(get("/api/incidencias/{id}", 999_999).header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void unUsuarioNoPuedeCambiarElEstado() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");

        mockMvc.perform(patch("/api/incidencias/{id}/estado", id)
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("estado", "EN_CURSO"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cicloDeVidaCompletoGestionadoPorUnTecnico() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");

        mockMvc.perform(patch("/api/incidencias/{id}/tecnico", id)
                        .header(HttpHeaders.AUTHORIZATION, tokenTecnico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("tecnicoId", tecnico.getId()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tecnico.email").value("tecnico@test.local"));

        cambiarEstado(id, "EN_CURSO").andExpect(status().isOk());
        cambiarEstado(id, "RESUELTA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaResolucion").exists());
        cambiarEstado(id, "CERRADA")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CERRADA"));
    }

    @Test
    void unaTransicionNoPermitidaDevuelve409() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");

        cambiarEstado(id, "RESUELTA")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void asignarAUnUsuarioSinRolTecnicoDevuelve400() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");
        Long idAna = usuarioRepository.findByEmail("ana@test.local").orElseThrow().getId();

        mockMvc.perform(patch("/api/incidencias/{id}/tecnico", id)
                        .header(HttpHeaders.AUTHORIZATION, tokenTecnico)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("tecnicoId", idAna))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void elCreadorPuedeEditarMientrasEstaAbiertaPeroNoDespues() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");
        var cambios = json(Map.of("titulo", "Sin red en la planta 3", "descripcion", "Desde esta mañana", "prioridad", "CRITICA"));

        mockMvc.perform(put("/api/incidencias/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cambios))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("Sin red en la planta 3"))
                .andExpect(jsonPath("$.prioridad").value("CRITICA"));

        cambiarEstado(id, "EN_CURSO").andExpect(status().isOk());

        mockMvc.perform(put("/api/incidencias/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, tokenAna)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cambios))
                .andExpect(status().isConflict());
    }

    @Test
    void comentariosDelCreadorYDelTecnico() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");

        comentar(tokenAna, id, "Sigue sin funcionar").andExpect(status().isCreated());
        comentar(tokenTecnico, id, "Lo reviso esta tarde").andExpect(status().isCreated());
        comentar(tokenPere, id, "Yo también opino").andExpect(status().isForbidden());

        mockMvc.perform(get("/api/incidencias/{id}/comentarios", id).header(HttpHeaders.AUTHORIZATION, tokenAna))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].texto").value("Sigue sin funcionar"))
                .andExpect(jsonPath("$[1].autor.email").value("tecnico@test.local"));
    }

    @Test
    void noSePuedeComentarUnaIncidenciaCerrada() throws Exception {
        long id = crearIncidencia(tokenAna, "Duplicada", "BAJA");
        cambiarEstado(id, "CERRADA").andExpect(status().isOk());

        comentar(tokenAna, id, "¿Por qué se ha cerrado?").andExpect(status().isConflict());
    }

    @Test
    void soloUnTecnicoPuedeEliminarYLuegoDevuelve404() throws Exception {
        long id = crearIncidencia(tokenAna, "Sin red", "ALTA");
        comentar(tokenAna, id, "Comentario que se borrará en cascada").andExpect(status().isCreated());

        mockMvc.perform(delete("/api/incidencias/{id}", id).header(HttpHeaders.AUTHORIZATION, tokenAna))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/incidencias/{id}", id).header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/incidencias/{id}", id).header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isNotFound());
    }

    @Test
    void soloLosTecnicosPuedenListarTecnicos() throws Exception {
        mockMvc.perform(get("/api/usuarios/tecnicos").header(HttpHeaders.AUTHORIZATION, tokenAna))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/usuarios/tecnicos").header(HttpHeaders.AUTHORIZATION, tokenTecnico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].rol").value("TECNICO"));
    }

    private long crearIncidencia(String token, String titulo, String prioridad) throws Exception {
        String respuesta = mockMvc.perform(post("/api/incidencias")
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("titulo", titulo, "descripcion", "Descripción de " + titulo,
                                "prioridad", prioridad))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return leer(respuesta).get("id").asLong();
    }

    private ResultActions cambiarEstado(long id, String estado) throws Exception {
        return mockMvc.perform(patch("/api/incidencias/{id}/estado", id)
                .header(HttpHeaders.AUTHORIZATION, tokenTecnico)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("estado", estado))));
    }

    private ResultActions comentar(String token, long id, String texto)
            throws Exception {
        return mockMvc.perform(post("/api/incidencias/{id}/comentarios", id)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("texto", texto))));
    }
}
