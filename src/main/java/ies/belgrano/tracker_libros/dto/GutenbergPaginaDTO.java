package ies.belgrano.tracker_libros.dto;

import java.util.List;

public record GutenbergPaginaDTO(List<GutenbergLibroDTO> libros, boolean haySiguiente) {}
