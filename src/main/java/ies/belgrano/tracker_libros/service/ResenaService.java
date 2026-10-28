package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.ResenaGuardarDTO;
import ies.belgrano.tracker_libros.dto.ResenaRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Resena;
import ies.belgrano.tracker_libros.repository.LibroRepository;
import ies.belgrano.tracker_libros.repository.ResenaRepository;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ResenaService {

    private final ResenaRepository resenaRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public ResenaService(
            ResenaRepository resenaRepository,
            LibroRepository libroRepository,
            UsuarioRepository usuarioRepository) {

        this.resenaRepository = resenaRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<ResenaRespuestaDTO> listar(Long usuarioId) {

        return resenaRepository.findByUsuario_Id(usuarioId)
                .stream()
                .map(this::convertir)
                .toList();
    }

    @Transactional(readOnly = true)
    public ResenaRespuestaDTO obtener(Long usuarioId, Long libroId) {

        return resenaRepository
                .findByUsuario_IdAndLibro_Id(usuarioId, libroId)
                .map(this::convertir)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resena no encontrada"));
    }

    @Transactional
    public ResenaRespuestaDTO guardar(
            Long usuarioId,
            Long libroId,
            ResenaGuardarDTO datos) {

        Resena resena = resenaRepository
                .findByUsuario_IdAndLibro_Id(usuarioId, libroId)
                .orElseGet(() -> {
                    Resena nueva = new Resena();
                    nueva.setUsuario(usuarioRepository.findById(usuarioId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Usuario no encontrado")));
                    nueva.setLibro(libroRepository.findById(libroId)
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Libro no encontrado")));
                    return nueva;
                });

        resena.setEstrellas(datos.estrellas());
        resena.setTexto(datos.texto() == null ? "" : datos.texto().trim());

        return convertir(resenaRepository.save(resena));
    }

    @Transactional
    public void eliminar(Long usuarioId, Long libroId) {

        resenaRepository.findByUsuario_IdAndLibro_Id(usuarioId, libroId)
                .ifPresent(resenaRepository::delete);
    }

    private ResenaRespuestaDTO convertir(Resena r) {

        return new ResenaRespuestaDTO(
                r.getLibro().getId(),
                r.getEstrellas(),
                r.getTexto(),
                r.getFechaActualizacion());
    }
}
