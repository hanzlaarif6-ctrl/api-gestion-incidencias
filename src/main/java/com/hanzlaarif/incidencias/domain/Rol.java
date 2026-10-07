package com.hanzlaarif.incidencias.domain;

/**
 * Roles de la aplicación.
 * <ul>
 *   <li>{@code USUARIO}: crea incidencias y solo ve las suyas.</li>
 *   <li>{@code TECNICO}: ve y gestiona todas las incidencias.</li>
 * </ul>
 */
public enum Rol {
    USUARIO,
    TECNICO
}
