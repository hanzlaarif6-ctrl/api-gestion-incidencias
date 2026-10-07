package com.hanzlaarif.incidencias.config;

import com.hanzlaarif.incidencias.domain.Comentario;
import com.hanzlaarif.incidencias.domain.EstadoIncidencia;
import com.hanzlaarif.incidencias.domain.Incidencia;
import com.hanzlaarif.incidencias.domain.Prioridad;
import com.hanzlaarif.incidencias.domain.Rol;
import com.hanzlaarif.incidencias.domain.Usuario;
import com.hanzlaarif.incidencias.repository.ComentarioRepository;
import com.hanzlaarif.incidencias.repository.IncidenciaRepository;
import com.hanzlaarif.incidencias.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga usuarios e incidencias de ejemplo al arrancar con el perfil {@code demo}
 * (activado en docker-compose), solo si la base de datos está vacía.
 * <p>
 * Es la única forma de crear técnicos en esta versión: el registro público
 * siempre crea usuarios con rol USUARIO.
 */
@Component
@Profile("demo")
public class DatosDemo implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosDemo.class);

    private final UsuarioRepository usuarioRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final ComentarioRepository comentarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DatosDemo(UsuarioRepository usuarioRepository, IncidenciaRepository incidenciaRepository,
                     ComentarioRepository comentarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.comentarioRepository = comentarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.count() > 0) {
            return;
        }
        Usuario tecnico = usuario("Laura Martí (soporte)", "tecnico@demo.local", "Tecnico123!", Rol.TECNICO);
        Usuario ana = usuario("Ana García", "ana@demo.local", "Usuario123!", Rol.USUARIO);
        Usuario pere = usuario("Pere Soler", "pere@demo.local", "Usuario123!", Rol.USUARIO);

        incidencia("No funciona la impresora de la segunda planta",
                "Al imprimir aparece el error de papel atascado aunque la bandeja está vacía.",
                Prioridad.MEDIA, ana, null, EstadoIncidencia.ABIERTA);
        Incidencia vpn = incidencia("No puedo conectarme a la VPN",
                "Desde casa la VPN se desconecta a los pocos segundos de conectar.",
                Prioridad.ALTA, ana, tecnico, EstadoIncidencia.EN_CURSO);
        incidencia("Solicitud de acceso a la carpeta compartida de Contratación",
                "Necesito permisos de lectura en la carpeta del departamento.",
                Prioridad.BAJA, pere, tecnico, EstadoIncidencia.RESUELTA);
        incidencia("El registro electrónico no carga",
                "La sede electrónica devuelve un error 500 al presentar una instancia.",
                Prioridad.CRITICA, pere, null, EstadoIncidencia.ABIERTA);

        comentarioRepository.save(new Comentario("¿Desde qué red te conectas? ¿Te pasa también con cable?", vpn, tecnico));
        comentarioRepository.save(new Comentario("Wifi de casa; con cable no lo he probado.", vpn, ana));

        log.info("Datos de demostración cargados: 3 usuarios, 4 incidencias, 2 comentarios");
    }

    private Usuario usuario(String nombre, String email, String password, Rol rol) {
        return usuarioRepository.save(new Usuario(nombre, email, passwordEncoder.encode(password), rol));
    }

    private Incidencia incidencia(String titulo, String descripcion, Prioridad prioridad, Usuario creador,
                                  Usuario tecnico, EstadoIncidencia estado) {
        Incidencia incidencia = new Incidencia(titulo, descripcion, prioridad, creador);
        incidencia.asignarTecnico(tecnico);
        // se recorre el ciclo de vida real hasta el estado pedido
        if (estado != EstadoIncidencia.ABIERTA) {
            incidencia.cambiarEstado(EstadoIncidencia.EN_CURSO);
        }
        if (estado == EstadoIncidencia.RESUELTA) {
            incidencia.cambiarEstado(EstadoIncidencia.RESUELTA);
        }
        return incidenciaRepository.save(incidencia);
    }
}
