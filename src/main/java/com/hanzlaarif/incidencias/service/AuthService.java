package com.hanzlaarif.incidencias.service;

import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.domain.Usuario;
import com.hanzlaarif.incidencias.exception.ConflictoException;
import com.hanzlaarif.incidencias.repository.UsuarioRepository;
import com.hanzlaarif.incidencias.security.JwtService;
import com.hanzlaarif.incidencias.security.UsuarioAutenticado;
import com.hanzlaarif.incidencias.web.dto.LoginRequest;
import com.hanzlaarif.incidencias.web.dto.LoginResponse;
import com.hanzlaarif.incidencias.web.dto.RegistroRequest;
import com.hanzlaarif.incidencias.web.dto.UsuarioResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /** El registro público siempre crea usuarios con rol USUARIO; los técnicos los da de alta la organización. */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new ConflictoException("Ya existe un usuario con el email " + email);
        }
        Usuario usuario = new Usuario(request.nombre().trim(), email,
                passwordEncoder.encode(request.password()), Rol.USUARIO);
        return UsuarioResponse.de(usuarioRepository.save(usuario));
    }

    /**
     * Comprueba las credenciales con el {@link AuthenticationManager} de Spring Security
     * (que usa BCrypt) y devuelve un JWT. Si son incorrectas lanza {@code BadCredentialsException} → 401.
     */
    public LoginResponse login(LoginRequest request) {
        Authentication autenticacion = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizarEmail(request.email()), request.password()));
        UsuarioAutenticado usuario = (UsuarioAutenticado) autenticacion.getPrincipal();
        return LoginResponse.bearer(jwtService.generarToken(usuario), jwtService.getExpiracionEnSegundos());
    }

    private static String normalizarEmail(String email) {
        return email.trim().toLowerCase();
    }
}
