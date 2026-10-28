package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SesionCrearDTO(
        @PositiveOrZero Integer minutos,
        @PositiveOrZero Integer paginasLeidas,
        @PositiveOrZero Integer paginaActual,
        @Size(max = 1000) String nota
) {
}