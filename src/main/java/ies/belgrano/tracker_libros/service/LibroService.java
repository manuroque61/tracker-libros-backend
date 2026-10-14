
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.entity.Libro;
import ies.belgrano.tracker_libros.repository.LibroRepository;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class LibroService {

    private final LibroRepository libroRepository;

    public LibroService(LibroRepository libroRepository) {
        this.libroRepository = libroRepository;
    }

    // Obtener todos los libros
    public List<Libro> obtenerTodos() {
        return libroRepository.findAll();
    }

    // Buscar un libro por ID
    public Libro obtenerPorId(Long id) {
        return libroRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Libro no encontrado"
                ));
    }

    // Guardar un libro nuevo
    public Libro guardar(Libro libro) {

        validarLibro(libro);

        if (libro.getIsbn() != null &&
                libroRepository.existsByIsbn(libro.getIsbn())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un libro con ese ISBN"
            );
        }

        libro.setId(null);

        return libroRepository.save(libro);
    }

    // Actualizar un libro existente
    public Libro actualizar(Long id, Libro datos) {

        Libro libro = obtenerPorId(id);

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

        return libroRepository.save(libro);
    }

    // Eliminar un libro
    public void eliminar(Long id) {

        Libro libro = obtenerPorId(id);

        libroRepository.delete(libro);
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

        if (libro.getCantidadPaginas() != null &&
                libro.getCantidadPaginas() <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La cantidad de paginas debe ser positiva"
            );
        }
    }
}
