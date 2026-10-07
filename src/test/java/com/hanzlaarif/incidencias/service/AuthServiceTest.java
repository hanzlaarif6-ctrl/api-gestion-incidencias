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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registrarGuardaElUsuarioConRolUsuarioYPasswordCifrada() {
        when(usuarioRepository.existsByEmail("ana@test.local")).thenReturn(false);
        when(passwordEncoder.encode("contrasena-segura")).thenReturn("HASH");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.registrar(new RegistroRequest(" Ana ", "  ANA@test.local ", "contrasena-segura"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getEmail()).isEqualTo("ana@test.local");
        assertThat(guardado.getValue().getNombre()).isEqualTo("Ana");
        assertThat(guardado.getValue().getPassword()).isEqualTo("HASH");
        assertThat(guardado.getValue().getRol()).isEqualTo(Rol.USUARIO);
    }

    @Test
    void registrarConEmailExistenteLanzaConflicto() {
        when(usuarioRepository.existsByEmail("ana@test.local")).thenReturn(true);

        assertThatThrownBy(() -> authService.registrar(new RegistroRequest("Ana", "ana@test.local", "contrasena-segura")))
                .isInstanceOf(ConflictoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void loginCorrectoDevuelveTokenBearer() {
        var usuario = new UsuarioAutenticado(1L, "ana@test.local", "HASH", Rol.USUARIO);
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities()));
        when(jwtService.generarToken(usuario)).thenReturn("token-jwt");
        when(jwtService.getExpiracionEnSegundos()).thenReturn(3600L);

        LoginResponse respuesta = authService.login(new LoginRequest("ana@test.local", "contrasena-segura"));

        assertThat(respuesta.token()).isEqualTo("token-jwt");
        assertThat(respuesta.tipo()).isEqualTo("Bearer");
        assertThat(respuesta.expiraEnSegundos()).isEqualTo(3600L);
    }

    @Test
    void loginIncorrectoPropagaBadCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("mal"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("ana@test.local", "incorrecta")))
                .isInstanceOf(BadCredentialsException.class);
    }
}
