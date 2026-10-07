package com.hanzlaarif.incidencias.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class EstadoIncidenciaTest {

    @ParameterizedTest(name = "{0} -> {1} permitido")
    @CsvSource({
            "ABIERTA, EN_CURSO",
            "ABIERTA, CERRADA",
            "EN_CURSO, RESUELTA",
            "EN_CURSO, ABIERTA",
            "RESUELTA, CERRADA",
            "RESUELTA, EN_CURSO"
    })
    void transicionesPermitidas(EstadoIncidencia origen, EstadoIncidencia destino) {
        assertThat(origen.puedePasarA(destino)).isTrue();
    }

    @ParameterizedTest(name = "{0} -> {1} no permitido")
    @CsvSource({
            "ABIERTA, RESUELTA",
            "EN_CURSO, CERRADA",
            "RESUELTA, ABIERTA"
    })
    void transicionesNoPermitidas(EstadoIncidencia origen, EstadoIncidencia destino) {
        assertThat(origen.puedePasarA(destino)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(EstadoIncidencia.class)
    void ningunEstadoPuedePasarASiMismo(EstadoIncidencia estado) {
        assertThat(estado.puedePasarA(estado)).isFalse();
    }

    @Test
    void cerradaEsEstadoFinal() {
        assertThat(EstadoIncidencia.CERRADA.siguientesPermitidos()).isEmpty();
    }
}
