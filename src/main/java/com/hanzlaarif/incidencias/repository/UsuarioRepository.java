package com.hanzlaarif.incidencias.repository;

import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Usuario> findByRolOrderByNombreAsc(Rol rol);
}
