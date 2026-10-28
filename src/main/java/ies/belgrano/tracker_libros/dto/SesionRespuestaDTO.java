package ies.belgrano.tracker_libros.dto;

import java.time.LocalDateTime;

public record SesionRespuestaDTO(
        Long id,
        Long lecturaId,
        LocalDateTime fecha,
        Integer minutos,
        Integer paginasLeidas,
        Integer paginaActual,
        String nota
) {
}