package com.hanzlaarif.incidencias.security;

import com.hanzlaarif.incidencias.domain.Rol;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRETO = "yZMYNw1OcZ4VY+n+H2H/B80FsFhOF9ostJPMVZk9sMs=";
    private static final String OTRO_SECRETO = "S8b6RBwcXii4AA+75YFGCWW8jiNAGKEZztrTaUVLpiQ=";
    private static final Instant AHORA = Instant.parse("2026-10-01T10:00:00Z");

    private final UsuarioAutenticado usuario = new UsuarioAutenticado(7L, "ana@test.local", "hash", Rol.USUARIO);

    private static JwtService servicio(String secreto, Instant instante) {
        return new JwtService(new JwtProperties(secreto, 60), Clock.fixed(instante, ZoneOffset.UTC));
    }

    @Test
    void unTokenGeneradoSeValidaYDevuelveElEmail() {
        JwtService jwt = servicio(SECRETO, AHORA);

        String token = jwt.generarToken(usuario);

        assertThat(jwt.extraerEmail(token)).isEqualTo("ana@test.local");
        assertThat(jwt.getExpiracionEnSegundos()).isEqualTo(3600);
    }

    @Test
    void unTokenCaducadoSeRechaza() {
        String token = servicio(SECRETO, AHORA).generarToken(usuario);
        JwtService dentroDeDosHoras = servicio(SECRETO, AHORA.plus(Duration.ofHours(2)));

        assertThatThrownBy(() -> dentroDeDosHoras.extraerEmail(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void unTokenFirmadoConOtraClaveSeRechaza() {
        String token = servicio(OTRO_SECRETO, AHORA).generarToken(usuario);

        assertThatThrownBy(() -> servicio(SECRETO, AHORA).extraerEmail(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void unTokenManipuladoSeRechaza() {
        JwtService jwt = servicio(SECRETO, AHORA);
        String token = jwt.generarToken(usuario);
        String manipulado = token.substring(0, token.length() - 3) + "abc";

        assertThatThrownBy(() -> jwt.extraerEmail(manipulado)).isInstanceOf(JwtException.class);
    }
}
