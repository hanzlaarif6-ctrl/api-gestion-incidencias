package com.hanzlaarif.incidencias.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Genera y valida tokens JWT firmados con HMAC-SHA256.
 * <p>
 * El token lleva el email en {@code sub} y, como información extra, el id y el rol.
 * La autorización no se fía de esos claims: en cada petición se vuelve a cargar el
 * usuario de la base de datos (ver {@link JwtAuthenticationFilter}).
 */
@Service
public class JwtService {

    private final SecretKey clave;
    private final Duration expiracion;
    private final Clock reloj;

    public JwtService(JwtProperties propiedades, Clock reloj) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(propiedades.secreto()));
        this.expiracion = Duration.ofMinutes(propiedades.expiracionMinutos());
        this.reloj = reloj;
    }

    public String generarToken(UsuarioAutenticado usuario) {
        Instant ahora = reloj.instant();
        return Jwts.builder()
                .subject(usuario.email())
                .claim("uid", usuario.id())
                .claim("rol", usuario.rol().name())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(expiracion)))
                .signWith(clave)
                .compact();
    }

    /**
     * Devuelve el email (subject) de un token válido.
     *
     * @throws JwtException si el token está mal formado, la firma no es válida o ha caducado
     */
    public String extraerEmail(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(clave)
                .clock(() -> Date.from(reloj.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    public long getExpiracionEnSegundos() {
        return expiracion.toSeconds();
    }
}
