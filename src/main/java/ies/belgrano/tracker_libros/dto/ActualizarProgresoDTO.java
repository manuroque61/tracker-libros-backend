
package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ActualizarProgresoDTO(

        @PositiveOrZero
        Integer paginaActual,

        @DecimalMin("0.0")
        @DecimalMax("100.0")
        Double porcentajeDigital,

        @Size(max = 1000)
        String ubicacionDigital

) {
}
