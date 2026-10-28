package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.EstadisticasDTO;
import ies.belgrano.tracker_libros.dto.EstadisticasDTO.FormatoConteo;
import ies.belgrano.tracker_libros.dto.EstadisticasDTO.MesConteo;
import ies.belgrano.tracker_libros.entity.Lectura;
import ies.belgrano.tracker_libros.entity.SesionLectura;
import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;
import ies.belgrano.tracker_libros.repository.LecturaRepository;
import ies.belgrano.tracker_libros.repository.SesionLecturaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        List<Lectura> todas = lecturaRepository.findByUsuario_Id(usuarioId);

        List<Lectura> lecturas = filtro == null
                ? todas
                : todas.stream()
                        .filter(l -> l.getFormato() == filtro)
                        .toList();

        Set<Long> ids = new HashSet<>();
        Set<Long> libros = new HashSet<>();
        for (Lectura l : lecturas) {
            ids.add(l.getId());
            libros.add(l.getLibro().getId());
        }

        List<SesionLectura> sesiones = sesionRepository
                .findByLectura_Usuario_Id(usuarioId)
                .stream()
                .filter(s -> ids.contains(s.getLectura().getId()))
                .toList();

        List<FormatoConteo> porFormato = new ArrayList<>();
        for (FormatoLectura formato : FormatoLectura.values()) {
            long total = todas.stream()
                    .filter(l -> l.getFormato() == formato)
                    .count();
            porFormato.add(new FormatoConteo(formato, total));
        }

        List<MesConteo> porMes = new ArrayList<>();
        YearMonth actual = YearMonth.now();
        for (int i = MESES - 1; i >= 0; i--) {
            YearMonth mes = actual.minusMonths(i);
            List<SesionLectura> delMes = sesiones.stream()
                    .filter(s -> YearMonth.from(s.getFecha()).equals(mes))
                    .toList();
            porMes.add(new MesConteo(
                    mes.toString(),
                    sumar(delMes, true),
                    sumar(delMes, false),
                    delMes.size()));
        }

        return new EstadisticasDTO(
                libros.size(),
                contar(lecturas, EstadoLectura.TERMINADO),
                contar(lecturas, EstadoLectura.LEYENDO),
                contar(lecturas, EstadoLectura.PENDIENTE),
                sumar(sesiones, true),
                sumar(sesiones, false),
                sesiones.size(),
                porFormato,
                porMes);
    }

    private long contar(List<Lectura> lecturas, EstadoLectura estado) {
        return lecturas.stream().filter(l -> l.getEstado() == estado).count();
    }

    private long sumar(List<SesionLectura> sesiones, boolean minutos) {
        return sesiones.stream()
                .mapToLong(s -> {
                    Integer valor = minutos ? s.getMinutos() : s.getPaginasLeidas();
                    return valor == null ? 0 : valor;
                })
                .sum();
    }
}
