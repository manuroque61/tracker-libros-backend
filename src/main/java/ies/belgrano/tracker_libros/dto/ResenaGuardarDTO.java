package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ResenaGuardarDTO(
        @NotNull @Min(1) @Max(5) Integer estrellas,
        String texto
) {
}