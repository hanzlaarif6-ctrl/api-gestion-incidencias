package com.hanzlaarif.incidencias.service;

import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Incidencia;
import com.hanzlaarif.incidencias.domain.Prioridad;
import com.hanzlaarif.incidencias.domain.Rol;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidenciaServiceTest {

    @Mock
    private IncidenciaRepository incidenciaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private IncidenciaService service;

    private Usuario ana;
    private Usuario tecnico;
    private UsuarioAutenticado anaAutenticada;
    private UsuarioAutenticado pereAutenticado;
    private UsuarioAutenticado tecnicoAutenticado;

    @BeforeEach
    void setUp() {
        ana = usuario(1L, "Ana", Rol.USUARIO);
        tecnico = usuario(3L, "Laura", Rol.TECNICO);
        anaAutenticada = UsuarioAutenticado.de(ana);
        pereAutenticado = UsuarioAutenticado.de(usuario(2L, "Pere", Rol.USUARIO));
        tecnicoAutenticado = UsuarioAutenticado.de(tecnico);
    }

    @Test
    void crearAsignaElCreadorYEmpiezaAbierta() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));
        when(incidenciaRepository.save(any(Incidencia.class))).thenAnswer(inv -> inv.getArgument(0));

        IncidenciaResponse creada = service.crear(
                new CrearIncidenciaRequest("  Sin red  ", "No hay conexión", Prioridad.ALTA), anaAutenticada);

        assertThat(creada.titulo()).isEqualTo("Sin red");
        assertThat(creada.estado()).isEqualTo(EstadoIncidencia.ABIERTA);
        assertThat(creada.creador().id()).isEqualTo(1L);
        assertThat(creada.tecnico()).isNull();
    }

    @Test
    void unUsuarioPuedeVerSuPropiaIncidencia() {
        Incidencia incidencia = incidenciaDe(ana, 10L);
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidencia));

        assertThat(service.obtener(10L, anaAutenticada).id()).isEqualTo(10L);
    }

    @Test
    void unUsuarioNoPuedeVerLaIncidenciaDeOtro() {
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidenciaDe(ana, 10L)));

        assertThatThrownBy(() -> service.obtener(10L, pereAutenticado)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void unTecnicoPuedeVerCualquierIncidencia() {
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidenciaDe(ana, 10L)));

        assertThat(service.obtener(10L, tecnicoAutenticado).creador().id()).isEqualTo(1L);
    }

    @Test
    void obtenerUnaIncidenciaInexistenteLanzaNoEncontrado() {
        when(incidenciaRepository.findWithUsuariosById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtener(99L, tecnicoAutenticado))
                .isInstanceOf(RecursoNoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void unUsuarioNoPuedeModificarSuIncidenciaSiYaNoEstaAbierta() {
        Incidencia incidencia = incidenciaDe(ana, 10L);
        incidencia.cambiarEstado(EstadoIncidencia.EN_CURSO);
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidencia));

        var cambios = new ActualizarIncidenciaRequest("Nuevo", "Nueva", Prioridad.BAJA);

        assertThatThrownBy(() -> service.actualizar(10L, cambios, anaAutenticada))
                .isInstanceOf(ConflictoException.class);
        verify(incidenciaRepository, never()).saveAndFlush(any());
    }

    @Test
    void cambiarEstadoConTransicionValidaActualizaElEstado() {
        Incidencia incidencia = incidenciaDe(ana, 10L);
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidencia));
        when(incidenciaRepository.saveAndFlush(incidencia)).thenReturn(incidencia);

        IncidenciaResponse respuesta = service.cambiarEstado(10L, EstadoIncidencia.EN_CURSO);

        assertThat(respuesta.estado()).isEqualTo(EstadoIncidencia.EN_CURSO);
    }

    @Test
    void cambiarEstadoConTransicionInvalidaLanzaConflicto() {
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidenciaDe(ana, 10L)));

        assertThatThrownBy(() -> service.cambiarEstado(10L, EstadoIncidencia.RESUELTA))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("ABIERTA")
                .hasMessageContaining("RESUELTA");
    }

    @Test
    void asignarAUnUsuarioQueNoEsTecnicoLanzaSolicitudNoValida() {
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidenciaDe(ana, 10L)));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> service.asignarTecnico(10L, 1L)).isInstanceOf(SolicitudNoValidaException.class);
    }

    @Test
    void asignarUnTecnicoLoGuardaEnLaIncidencia() {
        Incidencia incidencia = incidenciaDe(ana, 10L);
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidencia));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(tecnico));
        when(incidenciaRepository.saveAndFlush(incidencia)).thenReturn(incidencia);

        IncidenciaResponse respuesta = service.asignarTecnico(10L, 3L);

        assertThat(respuesta.tecnico().id()).isEqualTo(3L);
    }

    @Test
    void noSePuedeAsignarUnaIncidenciaCerrada() {
        Incidencia incidencia = incidenciaDe(ana, 10L);
        incidencia.cambiarEstado(EstadoIncidencia.CERRADA);
        when(incidenciaRepository.findWithUsuariosById(10L)).thenReturn(Optional.of(incidencia));

        assertThatThrownBy(() -> service.asignarTecnico(10L, 3L)).isInstanceOf(ConflictoException.class);
    }

    private static Usuario usuario(Long id, String nombre, Rol rol) {
        Usuario usuario = new Usuario(nombre, nombre.toLowerCase() + "@test.local", "hash", rol);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private static Incidencia incidenciaDe(Usuario creador, Long id) {
        Incidencia incidencia = new Incidencia("Sin red", "No hay conexión", Prioridad.MEDIA, creador);
        ReflectionTestUtils.setField(incidencia, "id", id);
        return incidencia;
    }
}
