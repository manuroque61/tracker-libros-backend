
package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record LibroCrearDTO(
    @NotBlank String titulo,
    @NotBlank String autor,
    String isbn,
    String genero,
    String descripcion,
    @Positive Integer cantidadPaginas,
    String editorial,
    String fechaPublicacion,
    String portadaUrl
) {}
