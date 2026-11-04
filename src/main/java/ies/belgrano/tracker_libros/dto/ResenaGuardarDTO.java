package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ResenaGuardarDTO(
        @NotNull @Min(1) @Max(5) Integer estrellas,
        @Size(max = 10000) String texto
) {
}
