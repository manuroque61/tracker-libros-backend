package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.TipoAnotacion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnotacionCrearDTO(
        @NotNull(message = "El libro es obligatorio") Long libroId,
        Long lecturaId,
        @NotNull(message = "El tipo es obligatorio") TipoAnotacion tipo,
        @NotBlank(message = "El texto es obligatorio") String texto,
        String cita,
        String ubicacion,
        Double porcentaje
) {
}