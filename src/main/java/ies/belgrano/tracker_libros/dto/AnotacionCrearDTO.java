package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.TipoAnotacion;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnotacionCrearDTO(
        @NotNull(message = "El libro es obligatorio") Long libroId,
        Long lecturaId,
        @NotNull(message = "El tipo es obligatorio") TipoAnotacion tipo,
        @NotBlank(message = "El texto es obligatorio")
        @Size(max = 10000) String texto,
        @Size(max = 10000) String cita,
        @Size(max = 1000) String ubicacion,
        @DecimalMin(value = "0", message = "El porcentaje debe ser 0 o mas")
        @DecimalMax(value = "100", message = "El porcentaje no puede superar 100")
        Double porcentaje
) {
}
