package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.FormatoLectura;

import java.util.List;

public record EstadisticasDTO(
        long totalLibros,
        long terminados,
        long leyendo,
        long pendientes,
        long minutosTotales,
        long paginasLeidas,
        long sesionesTotal,
        List<FormatoConteo> porFormato,
        List<MesConteo> porMes
) {
    public record FormatoConteo(FormatoLectura formato, long total) {
    }

    public record MesConteo(
            String clave,
            long minutos,
            long paginas,
            long sesiones) {
    }
}