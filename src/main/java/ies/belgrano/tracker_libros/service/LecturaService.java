
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.entity.Libro;
import ies.belgrano.tracker_libros.entity.Lectura;
import ies.belgrano.tracker_libros.entity.Usuario;

import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;

import ies.belgrano.tracker_libros.repository.LibroRepository;
import ies.belgrano.tracker_libros.repository.LecturaRepository;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class LecturaService {

    private final LecturaRepository lecturaRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    public LecturaService(
            LecturaRepository lecturaRepository,
            LibroRepository libroRepository,
            UsuarioRepository usuarioRepository) {

        this.lecturaRepository = lecturaRepository;
        this.libroRepository = libroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Obtener las lecturas de un usuario
    @Transactional(readOnly = true)
    public List<Lectura> obtenerPorUsuario(Long usuarioId) {
        return lecturaRepository.findByUsuario_Id(usuarioId);
    }

    // Obtener una lectura específica
    @Transactional(readOnly = true)
    public Lectura obtenerPorId(Long id, Long usuarioId) {
        return lecturaRepository.findByIdAndUsuario_Id(id, usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Lectura no encontrada"
                ));
    }

    // Registrar una lectura nueva
    @Transactional
    public Lectura registrar(
            Long usuarioId,
            Long libroId,
            FormatoLectura formato) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        Libro libro = libroRepository.findById(libroId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Libro no encontrado"
                ));

        if (formato == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El formato es obligatorio"
            );
        }

        if (lecturaRepository.existsByUsuario_IdAndLibro_IdAndFormato(
                usuarioId, libroId, formato)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El usuario ya tiene este libro en ese formato"
            );
        }

        Lectura lectura = new Lectura();

        lectura.setUsuario(usuario);
        lectura.setLibro(libro);
        lectura.setFormato(formato);
        lectura.setEstado(EstadoLectura.PENDIENTE);
        lectura.setPorcentajeProgreso(0.0);

        if (formato == FormatoLectura.FISICO) {
            lectura.setPaginaActual(0);
        }

        return lecturaRepository.save(lectura);
    }

    // Actualizar el progreso
    @Transactional
    public Lectura actualizarProgreso(
            Long id,
            Long usuarioId,
            Integer paginaActual,
            Double porcentajeDigital,
            String ubicacionDigital) {

        Lectura lectura = obtenerPorId(id, usuarioId);

        if (lectura.getFormato() == FormatoLectura.FISICO) {

            actualizarProgresoFisico(lectura, paginaActual);

        } else {

            actualizarProgresoDigital(
                    lectura,
                    porcentajeDigital,
                    ubicacionDigital
            );
        }

        actualizarEstadoAutomaticamente(lectura);

        return lecturaRepository.save(lectura);
    }

    private void actualizarProgresoFisico(
            Lectura lectura, Integer paginaActual) {

        Integer totalPaginas =
                lectura.getLibro().getCantidadPaginas();

        if (paginaActual == null ||
                paginaActual < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La pagina actual debe ser mayor o igual a cero"
            );
        }

        if (totalPaginas == null || totalPaginas <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El libro no tiene un total de paginas valido"
            );
        }

        if (paginaActual > totalPaginas) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La pagina actual supera el total del libro"
            );
        }

        double porcentaje =
                (paginaActual * 100.0) / totalPaginas;

        lectura.setPaginaActual(paginaActual);
        lectura.setPorcentajeProgreso(porcentaje);
    }

    private void actualizarProgresoDigital(
            Lectura lectura,
            Double porcentaje,
            String ubicacionDigital) {

        if (porcentaje == null ||
                !Double.isFinite(porcentaje) ||
                porcentaje < 0 ||
                porcentaje > 100) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El porcentaje debe estar entre 0 y 100"
            );
        }

        lectura.setPorcentajeProgreso(porcentaje);
        lectura.setUbicacionDigital(ubicacionDigital);
    }

    private void actualizarEstadoAutomaticamente(Lectura lectura) {

        double progreso = lectura.getPorcentajeProgreso();

        if (progreso >= 100) {

            lectura.setEstado(EstadoLectura.TERMINADO);

            if (lectura.getFechaInicio() == null) {
                lectura.setFechaInicio(LocalDate.now());
            }

            if (lectura.getFechaFinalizacion() == null) {
                lectura.setFechaFinalizacion(LocalDate.now());
            }

        } else if (progreso > 0) {

            lectura.setEstado(EstadoLectura.LEYENDO);

            if (lectura.getFechaInicio() == null) {
                lectura.setFechaInicio(LocalDate.now());
            }

            lectura.setFechaFinalizacion(null);

        } else {
            lectura.setEstado(EstadoLectura.PENDIENTE);
            lectura.setFechaFinalizacion(null);
        }
    }
}
