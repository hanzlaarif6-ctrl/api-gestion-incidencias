package com.hanzlaarif.incidencias.repository;

import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Incidencia;
import com.hanzlaarif.incidencias.domain.Prioridad;
import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.asignadaA;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.conEstado;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.conPrioridad;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.contieneTexto;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.creadaPor;
import static com.hanzlaarif.incidencias.repository.IncidenciaSpecifications.todas;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba las consultas dinámicas contra una base de datos real (H2 con el esquema de Flyway),
 * no contra mocks: aquí lo que importa es el SQL que genera Hibernate.
 */
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IncidenciaRepositoryTest {

    @Autowired
    private IncidenciaRepository incidenciaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario ana;
    private Usuario pere;
    private Usuario tecnico;

    @BeforeEach
    void setUp() {
        ana = usuarioRepository.save(new Usuario("Ana", "ana@test.local", "hash", Rol.USUARIO));
        pere = usuarioRepository.save(new Usuario("Pere", "pere@test.local", "hash", Rol.USUARIO));
        tecnico = usuarioRepository.save(new Usuario("Laura", "laura@test.local", "hash", Rol.TECNICO));

        guardar("Impresora atascada", "La impresora de la planta 2 no imprime", Prioridad.MEDIA, ana, null, false);
        guardar("VPN caída", "No conecta desde casa", Prioridad.ALTA, ana, tecnico, true);
        guardar("Permisos carpeta", "Necesito acceso a la carpeta de IMPRESIÓN", Prioridad.BAJA, pere, tecnico, true);
    }

    @Test
    void sinFiltrosDevuelveTodas() {
        assertThat(buscar(todas()).getTotalElements()).isEqualTo(3);
    }

    @Test
    void filtraPorCreador() {
        assertThat(buscar(todas(creadaPor(ana.getId()))).getContent())
                .extracting(Incidencia::getTitulo)
                .containsExactlyInAnyOrder("Impresora atascada", "VPN caída");
    }

    @Test
    void combinaVariosFiltrosConAnd() {
        Page<Incidencia> resultado = buscar(todas(
                conEstado(EstadoIncidencia.EN_CURSO),
                asignadaA(tecnico.getId()),
                conPrioridad(Prioridad.ALTA)));

        assertThat(resultado.getContent()).extracting(Incidencia::getTitulo).containsExactly("VPN caída");
    }

    @Test
    void lasCondicionesNulasSeIgnoran() {
        assertThat(buscar(todas(conEstado(null), conPrioridad(null), contieneTexto("  "))).getTotalElements())
                .isEqualTo(3);
    }

    @Test
    void buscaTextoEnTituloYDescripcionSinDistinguirMayusculas() {
        assertThat(buscar(todas(contieneTexto("impres"))).getContent())
                .extracting(Incidencia::getTitulo)
                .containsExactlyInAnyOrder("Impresora atascada", "Permisos carpeta");
    }

    @Test
    void paginaYOrdenaLosResultados() {
        Page<Incidencia> pagina = incidenciaRepository.findAll(todas(),
                PageRequest.of(0, 2, Sort.by("titulo")));

        assertThat(pagina.getTotalElements()).isEqualTo(3);
        assertThat(pagina.getTotalPages()).isEqualTo(2);
        assertThat(pagina.getContent()).extracting(Incidencia::getTitulo)
                .containsExactly("Impresora atascada", "Permisos carpeta");
    }

    private Page<Incidencia> buscar(org.springframework.data.jpa.domain.Specification<Incidencia> spec) {
        return incidenciaRepository.findAll(spec, PageRequest.of(0, 20));
    }

    private void guardar(String titulo, String descripcion, Prioridad prioridad, Usuario creador,
                         Usuario tecnicoAsignado, boolean enCurso) {
        Incidencia incidencia = new Incidencia(titulo, descripcion, prioridad, creador);
        incidencia.asignarTecnico(tecnicoAsignado);
        if (enCurso) {
            incidencia.cambiarEstado(EstadoIncidencia.EN_CURSO);
        }
        incidenciaRepository.save(incidencia);
    }
}
