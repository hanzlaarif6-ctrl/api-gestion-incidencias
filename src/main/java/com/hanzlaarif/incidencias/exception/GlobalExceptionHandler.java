package com.hanzlaarif.incidencias.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce las excepciones a respuestas HTTP con el formato estándar
 * Problem Details (RFC 9457, {@code application/problem+json}).
 * <p>
 * Al heredar de {@link ResponseEntityExceptionHandler} ya se cubren los errores propios de
 * Spring MVC: JSON mal formado o enum desconocido (400), parámetro con tipo incorrecto (400),
 * ruta inexistente (404), método no permitido (405), tipo de contenido no soportado (415)...
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    ProblemDetail noEncontrado(RecursoNoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictoException.class)
    ProblemDetail conflicto(ConflictoException e) {
        return problema(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(SolicitudNoValidaException.class)
    ProblemDetail solicitudNoValida(SolicitudNoValidaException e) {
        return problema(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    /** Credenciales incorrectas en el login. Mismo mensaje exista o no el email, para no revelar qué cuentas existen. */
    @ExceptionHandler(BadCredentialsException.class)
    ProblemDetail credencialesIncorrectas() {
        return problema(HttpStatus.UNAUTHORIZED, "Email o contraseña incorrectos");
    }

    /** Recurso ajeno o {@code @PreAuthorize} no superado. */
    @ExceptionHandler(AccessDeniedException.class)
    ProblemDetail accesoDenegado() {
        return problema(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta operación");
    }

    /** Restricción de base de datos (p. ej. email único en un registro concurrente). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integridad(DataIntegrityViolationException e) {
        log.warn("Violación de integridad de datos", e);
        return problema(HttpStatus.CONFLICT, "La operación entra en conflicto con los datos existentes");
    }

    /** Cualquier error no previsto: 500 sin filtrar detalles internos al cliente. */
    @ExceptionHandler(Exception.class)
    ProblemDetail errorInesperado(Exception e) {
        log.error("Error no controlado", e);
        return problema(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }

    /** Errores de Bean Validation (@Valid): se devuelve un mapa campo → mensaje. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos");
        problema.setProperty("errores", errores);
        return ResponseEntity.badRequest().body(problema);
    }

    /**
     * Cuerpo JSON ilegible: sintaxis incorrecta o un valor que no encaja con el tipo
     * (p. ej. una prioridad que no existe). Si es un enum, se indican los valores aceptados.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        String detalle = "El cuerpo de la petición no es un JSON válido";
        if (ex.getCause() instanceof InvalidFormatException formato && formato.getTargetType().isEnum()) {
            String campo = formato.getPath().isEmpty() ? "?" : formato.getPath().getLast().getFieldName();
            detalle = "Valor '" + formato.getValue() + "' no válido para '" + campo + "'. Valores aceptados: "
                    + Arrays.toString(formato.getTargetType().getEnumConstants());
        }
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, detalle));
    }

    private static ProblemDetail problema(HttpStatus estado, String detalle) {
        return ProblemDetail.forStatusAndDetail(estado, detalle);
    }
}
