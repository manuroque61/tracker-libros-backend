
package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LibroCrearDTO(
    @NotBlank @Size(max = 200) String titulo,
    @NotBlank @Size(max = 200) String autor,
    @Size(max = 20) String isbn,
    @Size(max = 100) String genero,
    @Size(max = 10000) String descripcion,
    @Positive Integer cantidadPaginas,
    @Size(max = 150) String editorial,
    @Size(max = 20) String fechaPublicacion,
    @Size(max = 1000) String portadaUrl
) {}
