package ies.belgrano.tracker_libros.dto;

public record LibroExternoDTO(
        String isbn,
        String titulo,
        String autor,
        String genero,
        String descripcion,
        Integer cantidadPaginas,
        String editorial,
        String fechaPublicacion,
        String portadaUrl
) {
}
