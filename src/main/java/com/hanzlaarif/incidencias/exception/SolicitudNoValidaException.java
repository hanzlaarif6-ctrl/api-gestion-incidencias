package com.hanzlaarif.incidencias.exception;

/** Datos con formato correcto pero que no cumplen una regla de negocio. Se traduce a 400 Bad Request. */
public class SolicitudNoValidaException extends RuntimeException {

    public SolicitudNoValidaException(String mensaje) {
        super(mensaje);
    }
}
