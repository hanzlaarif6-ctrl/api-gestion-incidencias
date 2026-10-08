package com.hanzlaarif.incidencias.web;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.oracle.OracleContainer;

/**
 * Ejecuta todos los tests de {@link IncidenciaIntegrationTest} contra una base de datos
 * <b>Oracle Database Free real</b> levantada en Docker con Testcontainers, con el perfil
 * {@code oracle} (migraciones de {@code db/oracle}).
 * <p>
 * Comprueba que las migraciones, la validación del esquema de Hibernate, los filtros con
 * Specifications, la paginación y el borrado en cascada funcionan también en Oracle.
 * Si no hay Docker disponible, el test se omite en lugar de fallar.
 */
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("oracle")
class IncidenciaOracleIntegrationTest extends IncidenciaIntegrationTest {

    @Container
    @ServiceConnection
    static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-free:23-slim-faststart");
}
