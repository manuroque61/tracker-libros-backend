
package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.EstadoLectura;
import jakarta.validation.constraints.NotNull;

public record ActualizarEstadoDTO(

        @NotNull(message = "El estado es obligatorio")
        EstadoLectura estado

) {
}
