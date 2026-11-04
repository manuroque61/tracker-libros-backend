package ies.belgrano.tracker_libros.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorRespuesta(
            Instant timestamp,
            int status,
            String error,
            String message) {}

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorRespuesta> manejarStatus(
            ResponseStatusException e) {

        HttpStatus estado = HttpStatus.resolve(e.getStatusCode().value());

        String mensaje = e.getReason() != null
                ? e.getReason()
                : "Error en la peticion";

        return ResponseEntity.status(e.getStatusCode())
                .body(new ErrorRespuesta(
                        Instant.now(),
                        e.getStatusCode().value(),
                        estado != null ? estado.getReasonPhrase() : "Error",
                        mensaje));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(
            MethodArgumentNotValidException e) {

        String mensaje = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " +
                        error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (mensaje.isBlank()) {
            mensaje = "Datos de la peticion invalidos";
        }

        return ResponseEntity.badRequest()
                .body(new ErrorRespuesta(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        mensaje));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarIntegridad(
            DataIntegrityViolationException e) {

        log.warn("Violacion de integridad: {}",
                e.getMostSpecificCause().getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorRespuesta(
                        Instant.now(),
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT.getReasonPhrase(),
                        "La operacion viola una restriccion de integridad"));
    }

    @ExceptionHandler({
            NoResourceFoundException.class,
            HttpRequestMethodNotSupportedException.class,
            HttpMediaTypeNotSupportedException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ErrorRespuesta> manejarErroresWeb(Exception e) {

        int status = HttpStatus.BAD_REQUEST.value();
        String mensaje = "Error en la peticion";

        if (e instanceof ErrorResponse respuesta) {
            status = respuesta.getStatusCode().value();

            if (respuesta.getBody() != null &&
                    respuesta.getBody().getDetail() != null) {
                mensaje = respuesta.getBody().getDetail();
            }
        } else if (e.getMessage() != null && !e.getMessage().isBlank()) {
            mensaje = e.getMessage();
        }

        HttpStatus estado = HttpStatus.resolve(status);

        return ResponseEntity.status(status)
                .body(new ErrorRespuesta(
                        Instant.now(),
                        status,
                        estado != null ? estado.getReasonPhrase() : "Error",
                        mensaje));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRespuesta> manejarGeneral(Exception e) {

        log.error("Error no controlado: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorRespuesta(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                        "Error interno del servidor"));
    }
}
