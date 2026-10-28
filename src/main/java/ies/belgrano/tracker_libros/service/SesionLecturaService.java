package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.SesionCrearDTO;
import ies.belgrano.tracker_libros.dto.SesionRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Lectura;
import ies.belgrano.tracker_libros.entity.SesionLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;
import ies.belgrano.tracker_libros.repository.SesionLecturaRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SesionLecturaService {

    private final SesionLecturaRepository sesionRepository;
    private final LecturaService lecturaService;

    public SesionLecturaService(
            SesionLecturaRepository sesionRepository,
            LecturaService lecturaService) {

        this.sesionRepository = sesionRepository;
        this.lecturaService = lecturaService;
    }

    @Transactional(readOnly = true)
    public List<SesionRespuestaDTO> listar(Long lecturaId, Long usuarioId) {

        lecturaService.obtenerPorId(lecturaId, usuarioId);

        return sesionRepository
                .findByLectura_IdOrderByFechaDesc(lecturaId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional
    public SesionRespuestaDTO registrar(
            Long lecturaId,
            Long usuarioId,
            SesionCrearDTO datos) {

        Lectura lectura = lecturaService.obtenerPorId(lecturaId, usuarioId);

        SesionLectura sesion = new SesionLectura();
        sesion.setLectura(lectura);
        sesion.setMinutos(datos.minutos());
        sesion.setPaginasLeidas(datos.paginasLeidas());
        sesion.setPaginaActual(datos.paginaActual());
        sesion.setNota(datos.nota());

        SesionLectura guardada = sesionRepository.save(sesion);

        // En lecturas fisicas la sesion tambien actualiza el progreso
        if (lectura.getFormato() == FormatoLectura.FISICO
                && datos.paginaActual() != null) {

            lecturaService.actualizarProgreso(
                    lecturaId, usuarioId,
                    datos.paginaActual(), null, null);
        }

        return convertir(guardada);
    }

    private SesionRespuestaDTO convertir(SesionLectura sesion) {

        return new SesionRespuestaDTO(
                sesion.getId(),
                sesion.getLectura().getId(),
                sesion.getFecha(),
                sesion.getMinutos(),
                sesion.getPaginasLeidas(),
                sesion.getPaginaActual(),
                sesion.getNota());
    }
}
