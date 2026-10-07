package com.hanzlaarif.incidencias.domain;

import java.util.Set;

/**
 * Ciclo de vida de una incidencia.
 *
 * <pre>
 *   ABIERTA  → EN_CURSO | CERRADA (descartada)
 *   EN_CURSO → RESUELTA | ABIERTA (se libera)
 *   RESUELTA → CERRADA  | EN_CURSO (se reabre)
 *   CERRADA  → (estado final)
 * </pre>
 *
 * Las transiciones permitidas se definen en el propio enum para que la regla
 * de negocio esté en un único sitio y se pueda probar de forma aislada.
 */
public enum EstadoIncidencia {
    ABIERTA,
    EN_CURSO,
    RESUELTA,
    CERRADA;

    public Set<EstadoIncidencia> siguientesPermitidos() {
        return switch (this) {
            case ABIERTA -> Set.of(EN_CURSO, CERRADA);
            case EN_CURSO -> Set.of(ABIERTA, RESUELTA);
            case RESUELTA -> Set.of(EN_CURSO, CERRADA);
            case CERRADA -> Set.of();
        };
    }

    public boolean puedePasarA(EstadoIncidencia destino) {
        return siguientesPermitidos().contains(destino);
    }
}
