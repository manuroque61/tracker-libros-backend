
package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.FormatoLectura;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgregarLibroBibliotecaDTO(
        @NotBlank String titulo,
        @NotBlank String autor,
        String isbn,
        String genero,
        String descripcion,
        @Positive Integer cantidadPaginas,
        String editorial,
        String fechaPublicacion,
        String portadaUrl,
        @NotNull FormatoLectura formato
) {
}
