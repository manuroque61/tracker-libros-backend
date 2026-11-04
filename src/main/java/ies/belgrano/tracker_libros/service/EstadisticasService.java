package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.EstadisticasDTO;
import ies.belgrano.tracker_libros.dto.EstadisticasDTO.FormatoConteo;
import ies.belgrano.tracker_libros.dto.EstadisticasDTO.MesConteo;
import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;
import ies.belgrano.tracker_libros.repository.LecturaRepository;
import ies.belgrano.tracker_libros.repository.SesionLecturaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EstadisticasService {

    private static final int MESES = 6;

    private final LecturaRepository lecturaRepository;
    private final SesionLecturaRepository sesionRepository;

    public EstadisticasService(
            LecturaRepository lecturaRepository,
            SesionLecturaRepository sesionRepository) {

        this.lecturaRepository = lecturaRepository;
        this.sesionRepository = sesionRepository;
    }

    @Transactional(readOnly = true)
    public EstadisticasDTO obtener(Long usuarioId, FormatoLectura filtro) {

        long totalLibros =
                lecturaRepository.contarLibrosDistintos(usuarioId, filtro);

        long terminados = 0;
        long leyendo = 0;
        long pendientes = 0;

        for (Object[] fila : lecturaRepository.contarPorEstado(
                usuarioId, filtro)) {

            EstadoLectura estado = (EstadoLectura) fila[0];
            long total = ((Number) fila[1]).longValue();

            if (estado == EstadoLectura.TERMINADO) {
                terminados = total;
            } else if (estado == EstadoLectura.LEYENDO) {
                leyendo = total;
            } else if (estado == EstadoLectura.PENDIENTE) {
                pendientes = total;
            }
        }

        Map<FormatoLectura, Long> conteos =
                new EnumMap<>(FormatoLectura.class);

        for (Object[] fila : lecturaRepository.contarPorFormato(usuarioId)) {
            conteos.put(
                    (FormatoLectura) fila[0],
                    ((Number) fila[1]).longValue());
        }

        List<FormatoConteo> porFormato = new ArrayList<>();
        for (FormatoLectura formato : FormatoLectura.values()) {
            porFormato.add(new FormatoConteo(
                    formato,
                    conteos.getOrDefault(formato, 0L)));
        }

        long minutosTotales = 0;
        long paginasLeidas = 0;
        long sesionesTotal = 0;

        List<Object[]> filasTotales =
                sesionRepository.sumarTotales(usuarioId, filtro);

        if (!filasTotales.isEmpty()) {
            Object[] totales = filasTotales.get(0);

            if (totales.length >= 3) {
                minutosTotales = ((Number) totales[0]).longValue();
                paginasLeidas = ((Number) totales[1]).longValue();
                sesionesTotal = ((Number) totales[2]).longValue();
            }
        }

        YearMonth actual = YearMonth.now();
        YearMonth primerMes = actual.minusMonths(MESES - 1);

        Map<YearMonth, long[]> porMesMap = new HashMap<>();

        LocalDateTime desde = primerMes.atDay(1).atStartOfDay();

        for (Object[] fila : sesionRepository.sumarPorMes(
                usuarioId, desde, filtro)) {

            int anio = ((Number) fila[0]).intValue();
            int mes = ((Number) fila[1]).intValue();

            YearMonth clave = YearMonth.of(anio, mes);

            if (clave.isBefore(primerMes) || clave.isAfter(actual)) {
                continue;
            }

            porMesMap.put(clave, new long[]{
                    ((Number) fila[2]).longValue(),
                    ((Number) fila[3]).longValue(),
                    ((Number) fila[4]).longValue()});
        }

        List<MesConteo> porMes = new ArrayList<>();
        for (int i = MESES - 1; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            long[] valores = porMesMap.get(mes);

            porMes.add(new MesConteo(
                    mes.toString(),
                    valores != null ? valores[0] : 0,
                    valores != null ? valores[1] : 0,
                    valores != null ? valores[2] : 0));
        }

        return new EstadisticasDTO(
                totalLibros,
                terminados,
                leyendo,
                pendientes,
                minutosTotales,
                paginasLeidas,
                sesionesTotal,
                porFormato,
                porMes);
    }
}
