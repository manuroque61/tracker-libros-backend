
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.AgregarLibroBibliotecaDTO;
import ies.belgrano.tracker_libros.dto.LecturaRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Libro;
import ies.belgrano.tracker_libros.repository.LibroRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class BibliotecaAltaService {

    private final LibroRepository libroRepository;
    private final LibroService libroService;
    private final LecturaService lecturaService;

    public BibliotecaAltaService(
            LibroRepository libroRepository,
            LibroService libroService,
            LecturaService lecturaService) {

        this.libroRepository = libroRepository;
        this.libroService = libroService;
        this.lecturaService = lecturaService;
    }

    @Transactional
    public LecturaRespuestaDTO agregarLibro(
            Long usuarioId,
            AgregarLibroBibliotecaDTO datos) {

        String isbn = datos.isbn() == null
                ? null
                : datos.isbn().trim();

        if (isbn != null && isbn.isBlank()) {
            isbn = null;
        }

        // Buscar primero en el catalogo compartido.
        Optional<Libro> existente = isbn == null
                ? Optional.empty()
                : libroRepository.findByIsbn(isbn);

        Libro libro;

        if (existente.isPresent()) {
            // Reutilizamos el registro bibliografico.
            libro = existente.get();
        } else {
            // Si no existe, creamos uno nuevo.
            libro = new Libro();

            libro.setTitulo(datos.titulo().trim());
            libro.setAutor(datos.autor().trim());
            libro.setIsbn(isbn);
            libro.setGenero(datos.genero());
            libro.setDescripcion(datos.descripcion());
            libro.setCantidadPaginas(datos.cantidadPaginas());
            libro.setEditorial(datos.editorial());
            libro.setPortadaUrl(datos.portadaUrl());

            libro.setFechaPublicacion(
                    convertirFecha(datos.fechaPublicacion())
            );

            libro = libroService.guardar(libro);
        }

        // Crear una lectura personal del usuario.
        return lecturaService.registrarEnBiblioteca(
                usuarioId,
                libro.getId(),
                datos.formato()
        );
    }

    private LocalDate convertirFecha(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        String fecha = valor.trim();

        // No inventamos dia ni mes cuando solo
        // conocemos el año.
        if (fecha.matches("\\d{4}")) {
            return null;
        }

        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha debe usar AAAA o AAAA-MM-DD"
            );
        }
    }
}
