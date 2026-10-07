package com.hanzlaarif.incidencias.repository;

import com.hanzlaarif.incidencias.domain.Comentario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    @EntityGraph(attributePaths = "autor")
    List<Comentario> findByIncidenciaIdOrderByFechaAscIdAsc(Long incidenciaId);
}
