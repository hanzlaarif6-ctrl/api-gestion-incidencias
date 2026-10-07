package com.hanzlaarif.incidencias.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncidenciaTest {

    private final Usuario creador = new Usuario("Ana", "ana@test.local", "hash", Rol.USUARIO);

    @Test
    void unaIncidenciaNuevaEstaAbiertaYSinTecnico() {
        Incidencia incidencia = new Incidencia("Título", "Descripción", Prioridad.ALTA, creador);

        assertThat(incidencia.getEstado()).isEqualTo(EstadoIncidencia.ABIERTA);
        assertThat(incidencia.getTecnico()).isNull();
        assertThat(incidencia.getFechaResolucion()).isNull();
    }

    @Test
    void alResolverSeGuardaLaFechaDeResolucionYAlReabrirSeBorra() {
        Incidencia incidencia = new Incidencia("Título", "Descripción", Prioridad.ALTA, creador);
        incidencia.cambiarEstado(EstadoIncidencia.EN_CURSO);

        incidencia.cambiarEstado(EstadoIncidencia.RESUELTA);
        assertThat(incidencia.getFechaResolucion()).isNotNull();

        incidencia.cambiarEstado(EstadoIncidencia.EN_CURSO);
        assertThat(incidencia.getFechaResolucion()).isNull();
    }

    @Test
    void alCerrarUnaIncidenciaResueltaSeConservaLaFechaDeResolucion() {
        Incidencia incidencia = new Incidencia("Título", "Descripción", Prioridad.ALTA, creador);
        incidencia.cambiarEstado(EstadoIncidencia.EN_CURSO);
        incidencia.cambiarEstado(EstadoIncidencia.RESUELTA);
        var fechaResolucion = incidencia.getFechaResolucion();

        incidencia.cambiarEstado(EstadoIncidencia.CERRADA);

        assertThat(incidencia.getFechaResolucion()).isEqualTo(fechaResolucion);
    }
}
