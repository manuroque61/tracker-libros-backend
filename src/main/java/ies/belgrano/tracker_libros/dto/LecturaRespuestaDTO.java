package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LecturaRespuestaDTO(
        Long id,
        Long libroId,
        String titulo,
        String autor,
        String portadaUrl,
        FormatoLectura formato,
        EstadoLectura estado,
        Integer paginaActual,
        Double porcentajeProgreso,
        String ubicacionDigital,
        LocalDate fechaInicio,
        LocalDate fechaFinalizacion,
        LocalDateTime fechaAgregado
) {
}