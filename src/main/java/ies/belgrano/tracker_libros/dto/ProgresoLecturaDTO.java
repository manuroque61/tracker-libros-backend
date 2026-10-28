package ies.belgrano.tracker_libros.dto;

public record ProgresoLecturaDTO(
        Integer paginaActual,
        Double porcentaje,
        String ubicacionDigital
) {
}