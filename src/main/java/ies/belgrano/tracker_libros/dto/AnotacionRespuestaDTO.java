package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.TipoAnotacion;

import java.time.LocalDateTime;

public record AnotacionRespuestaDTO(
        Long id,
        Long libroId,
        Long lecturaId,
        TipoAnotacion tipo,
        String texto,
        String cita,
        String ubicacion,
        Double porcentaje,
        LocalDateTime creadoEn
) {
}