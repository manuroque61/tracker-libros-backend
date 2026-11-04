
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.entity.Libro;
import ies.belgrano.tracker_libros.repository.AnotacionRepository;
import ies.belgrano.tracker_libros.repository.LecturaRepository;
import ies.belgrano.tracker_libros.repository.LibroRepository;
import ies.belgrano.tracker_libros.repository.ResenaRepository;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class LibroService {

    private final LibroRepository libroRepository;
    private final LecturaRepository lecturaRepository;
    private final ResenaRepository resenaRepository;
    private final AnotacionRepository anotacionRepository;

    public LibroService(
            LibroRepository libroRepository,
            LecturaRepository lecturaRepository,
            ResenaRepository resenaRepository,
            AnotacionRepository anotacionRepository) {
        this.libroRepository = libroRepository;
        this.lecturaRepository = lecturaRepository;
        this.resenaRepository = resenaRepository;
        this.anotacionRepository = anotacionRepository;
    }

    // Obtener todos los libros
    @Transactional(readOnly = true)
    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    // Buscar un libro por ID
    @Transactional(readOnly = true)
    public Libro obtenerPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Libro no encontrado"
                ));
    }

    // Guardar un libro nuevo
    @Transactional
    public Libro guardar(Libro libro) {

        libro.setIsbn(normalizarIsbn(libro.getIsbn()));

        validarLibro(libro);

        if (libro.getIsbn() != null &&
                libroRepository.existsByIsbn(libro.getIsbn())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un libro con ese ISBN"
            );
        }

        libro.setId(null);

        try {
            return libroRepository.save(libro);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un libro con ese ISBN"
            );
        }
    }

    // Actualizar un libro existente
    @Transactional
    public Libro actualizar(Long id, Libro datos) {

        Libro libro = obtenerPorId(id);

        datos.setIsbn(normalizarIsbn(datos.getIsbn()));

        validarLibro(datos);

        if (datos.getIsbn() != null) {
            libroRepository.findByIsbn(datos.getIsbn())
                    .filter(encontrado ->
                            !encontrado.getId().equals(id))
                    .ifPresent(encontrado -> {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El ISBN pertenece a otro libro"
                        );
                    });
        }

        libro.setTitulo(datos.getTitulo());
        libro.setAutor(datos.getAutor());
        libro.setGenero(datos.getGenero());
        libro.setDescripcion(datos.getDescripcion());
        libro.setCantidadPaginas(datos.getCantidadPaginas());
        libro.setEditorial(datos.getEditorial());
        libro.setIsbn(datos.getIsbn());
        libro.setFechaPublicacion(datos.getFechaPublicacion());
        libro.setPortadaUrl(datos.getPortadaUrl());

        try {
            return libroRepository.save(libro);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El ISBN pertenece a otro libro"
            );
        }
    }

    // Eliminar un libro
    @Transactional
    public void eliminar(Long id) {

        Libro libro = obtenerPorId(id);

        if (lecturaRepository.existsByLibro_Id(id) ||
                resenaRepository.existsByLibro_Id(id) ||
                anotacionRepository.existsByLibro_Id(id)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un libro con lecturas, resenas o anotaciones asociadas"
            );
        }

        try {
            libroRepository.delete(libro);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No se puede eliminar un libro con datos asociados"
            );
        }
    }

    // Validaciones de negocio
    private void validarLibro(Libro libro) {

        if (libro.getTitulo() == null ||
                libro.getTitulo().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El titulo es obligatorio"
            );
        }

        if (libro.getAutor() == null ||
                libro.getAutor().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El autor es obligatorio"
            );
        }

        if (libro.getIsbn() != null &&
                !libro.getIsbn().matches("(?:\\d{13}|\\d{9}[\\dX])")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ISBN debe tener 10 o 13 caracteres"
            );
        }

        if (libro.getCantidadPaginas() != null &&
                libro.getCantidadPaginas() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de paginas debe ser positiva"
            );
        }
    }

    private String normalizarIsbn(String isbn) {

        if (isbn == null) {
            return null;
        }

        String limpio = isbn
                .replace("-", "")
                .replace(" ", "")
                .trim()
                .toUpperCase(Locale.ROOT);

        return limpio.isEmpty() ? null : limpio;
    }
}
