package ies.belgrano.tracker_libros.dto;

import java.util.List;

public record GutenbergLibroDTO(
        String id,
        String titulo,
        List<String> autores,
        String idioma,
        List<String> temas,
        String isbn,
        String fechaPublicacion,
        String descripcion,
        String cantidadPaginas,
        String portadaUrl,
        String urlEpub,
        String urlPdf,
        String coverUrl
) {
}
