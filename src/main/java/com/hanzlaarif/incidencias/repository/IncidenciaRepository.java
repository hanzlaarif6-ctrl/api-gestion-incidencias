package com.hanzlaarif.incidencias.repository;

import com.hanzlaarif.incidencias.domain.Incidencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IncidenciaRepository extends JpaRepository<Incidencia, Long>, JpaSpecificationExecutor<Incidencia> {

    /**
     * Carga creador y técnico en la misma consulta (JOIN) para evitar el problema N+1
     * al convertir la página de resultados a DTO.
     */
    @Override
    @EntityGraph(attributePaths = {"creador", "tecnico"})
    Page<Incidencia> findAll(Specification<Incidencia> spec, Pageable pageable);

    @EntityGraph(attributePaths = {"creador", "tecnico"})
    Optional<Incidencia> findWithUsuariosById(Long id);
}
