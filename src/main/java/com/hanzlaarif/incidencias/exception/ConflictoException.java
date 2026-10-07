package com.hanzlaarif.incidencias.exception;

/**
 * La petición es correcta pero choca con el estado actual del recurso
 * (email ya registrado, transición de estado no permitida...). Se traduce a 409 Conflict.
 */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
