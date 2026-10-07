package com.hanzlaarif.incidencias.repository;

import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Incidencia;
import com.hanzlaarif.incidencias.domain.Prioridad;
import org.springframework.data.jpa.domain.Specification;

/**
 * Filtros dinámicos para el listado de incidencias.
 * Cada método devuelve una condición o {@code null} si el filtro no se ha indicado,
 * y {@link #todas} combina con AND solo las que no son {@code null}. Así una única
 * consulta sirve para cualquier combinación de filtros.
 */
public final class IncidenciaSpecifications {

    private IncidenciaSpecifications() {
    }

    @SafeVarargs
    public static Specification<Incidencia> todas(Specification<Incidencia>... filtros) {
        Specification<Incidencia> resultado = (root, query, cb) -> cb.conjunction();
        for (Specification<Incidencia> filtro : filtros) {
            if (filtro != null) {
                resultado = resultado.and(filtro);
            }
        }
        return resultado;
    }

    public static Specification<Incidencia> conEstado(EstadoIncidencia estado) {
        return estado == null ? null : (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    public static Specification<Incidencia> conPrioridad(Prioridad prioridad) {
        return prioridad == null ? null : (root, query, cb) -> cb.equal(root.get("prioridad"), prioridad);
    }

    public static Specification<Incidencia> creadaPor(Long usuarioId) {
        return usuarioId == null ? null : (root, query, cb) -> cb.equal(root.get("creador").get("id"), usuarioId);
    }

    public static Specification<Incidencia> asignadaA(Long tecnicoId) {
        return tecnicoId == null ? null : (root, query, cb) -> cb.equal(root.get("tecnico").get("id"), tecnicoId);
    }

    /** Búsqueda de texto, sin distinguir mayúsculas, en título y descripción. */
    public static Specification<Incidencia> contieneTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String patron = "%" + texto.trim().toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titulo")), patron),
                cb.like(cb.lower(root.get("descripcion")), patron));
    }
}
