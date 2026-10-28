package ies.belgrano.tracker_libros.dto;

import java.time.LocalDateTime;

public record ResenaRespuestaDTO(
        Long libroId,
        Integer estrellas,
        String texto,
        LocalDateTime actualizadoEn
) {
}