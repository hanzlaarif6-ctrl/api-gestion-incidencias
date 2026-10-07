package com.hanzlaarif.incidencias.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.domain.Usuario;
import com.hanzlaarif.incidencias.repository.ComentarioRepository;
import com.hanzlaarif.incidencias.repository.IncidenciaRepository;
import com.hanzlaarif.incidencias.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base de los tests de integración: arranca la aplicación completa (seguridad, validación,
 * JPA, Flyway sobre H2) y hace peticiones HTTP simuladas con MockMvc.
 * Las tablas se vacían antes de cada test para que sean independientes entre sí.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
abstract class IntegrationTestBase {

    protected static final String PASSWORD = "contrasena-segura";

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected UsuarioRepository usuarioRepository;
    @Autowired
    private IncidenciaRepository incidenciaRepository;
    @Autowired
    private ComentarioRepository comentarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limpiarBaseDeDatos() {
        comentarioRepository.deleteAll();
        incidenciaRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    /** Los técnicos no se pueden registrar por la API, así que se crean directamente en la base de datos. */
    protected Usuario crearTecnico(String email) {
        return usuarioRepository.save(new Usuario("Técnico", email, passwordEncoder.encode(PASSWORD), Rol.TECNICO));
    }

    protected void registrar(String email) throws Exception {
        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("nombre", "Usuario de prueba", "email", email, "password", PASSWORD))))
                .andExpect(status().isCreated());
    }

    protected String login(String email) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("email", email, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return "Bearer " + objectMapper.readTree(respuesta).get("token").asText();
    }

    protected String registrarYLogin(String email) throws Exception {
        registrar(email);
        return login(email);
    }

    protected String json(Object objeto) throws Exception {
        return objectMapper.writeValueAsString(objeto);
    }

    protected JsonNode leer(String json) throws Exception {
        return objectMapper.readTree(json);
    }
}
