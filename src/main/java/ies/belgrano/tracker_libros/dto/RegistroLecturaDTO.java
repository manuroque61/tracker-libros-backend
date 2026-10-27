
package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.FormatoLectura;

import jakarta.validation.constraints.NotNull;

public record RegistroLecturaDTO(

    @NotNull(message = "El libro es obligatorio")
    Long libroId,

    @NotNull(message = "El formato es obligatorio")
    FormatoLectura formato

) {
}

