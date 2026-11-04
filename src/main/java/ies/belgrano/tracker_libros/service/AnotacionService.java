package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.AnotacionCrearDTO;
import ies.belgrano.tracker_libros.dto.AnotacionRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Anotacion;
import ies.belgrano.tracker_libros.entity.Lectura;
import ies.belgrano.tracker_libros.repository.AnotacionRepository;
import ies.belgrano.tracker_libros.repository.LecturaRepository;
import ies.belgrano.tracker_libros.repository.LibroRepository;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AnotacionService {

    private final AnotacionRepository anotacionRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;
    private final LecturaRepository lecturaRepository;

    public AnotacionService(
            AnotacionRepository anotacionRepository,
            LibroRepository libroRepository,
            UsuarioRepository usuarioRepository,
            LecturaRepository lecturaRepository) {

        this.anotacionRepository = anotacionRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
        this.lecturaRepository = lecturaRepository;
    }

    @Transactional(readOnly = true)
    public List<AnotacionRespuestaDTO> listarPorLibro(
            Long usuarioId,
            Long libroId) {

        return anotacionRepository
                .findByUsuario_IdAndLibro_IdOrderByFechaCreacionDesc(
                        usuarioId, libroId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional
    public AnotacionRespuestaDTO crear(
            Long usuarioId,
            Long libroId,
            AnotacionCrearDTO datos) {

        Anotacion anotacion = new Anotacion();

        anotacion.setUsuario(usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado")));

        anotacion.setLibro(libroRepository.findById(libroId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Libro no encontrado")));

        if (datos.lecturaId() != null) {
            Lectura lectura = lecturaRepository
                    .findByIdAndUsuario_Id(datos.lecturaId(), usuarioId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Lectura no encontrada"));

            if (!lectura.getLibro().getId().equals(libroId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "La lectura no pertenece a ese libro");
            }

            anotacion.setLecturaId(lectura.getId());
        }

        anotacion.setTipo(datos.tipo());
        anotacion.setTexto(datos.texto());
        anotacion.setCita(datos.cita());
        anotacion.setUbicacion(datos.ubicacion());
        anotacion.setPorcentaje(datos.porcentaje());

        return convertir(anotacionRepository.save(anotacion));
    }

    @Transactional
    public void eliminar(Long id, Long usuarioId) {

        Anotacion anotacion = anotacionRepository
                .findByIdAndUsuario_Id(id, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Anotacion no encontrada"));

        anotacionRepository.delete(anotacion);
    }

    private AnotacionRespuestaDTO convertir(Anotacion a) {

        return new AnotacionRespuestaDTO(
                a.getId(),
                a.getLibro().getId(),
                a.getLecturaId(),
                a.getTipo(),
                a.getTexto(),
                a.getCita(),
                a.getUbicacion(),
                a.getPorcentaje(),
                a.getFechaCreacion());
    }
}
