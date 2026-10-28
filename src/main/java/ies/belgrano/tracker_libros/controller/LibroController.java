package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.LibroCrearDTO;
import ies.belgrano.tracker_libros.dto.LibroExternoDTO;
import ies.belgrano.tracker_libros.entity.Libro;
import ies.belgrano.tracker_libros.service.LibroExternoService;
import ies.belgrano.tracker_libros.service.LibroService;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroExternoService libroExternoService;
    private final LibroService libroService;

    public LibroController(
            LibroExternoService libroExternoService,
            LibroService libroService) {
        this.libroExternoService = libroExternoService;
        this.libroService = libroService;
    }

    @GetMapping("/buscar-isbn/{isbn}")
    public ResponseEntity<LibroExternoDTO> buscarPorIsbn(
            @PathVariable String isbn) {

        if (!isbn.matches("(?:\\d{13}|\\d{9}[\\dXx])")) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(
            libroExternoService.buscarPorIsbn(isbn)
        );
    }

    @GetMapping
    public ResponseEntity<Iterable<Libro>> listar() {

        return ResponseEntity.ok(libroService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Libro> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
            libroService.obtenerPorId(id)
        );
    }

    @PostMapping
    public ResponseEntity<Libro> crear(
            @Valid @RequestBody LibroCrearDTO datos) {

        Libro libro = libroDesdeDTO(datos);

        Libro guardado = libroService.guardar(libro);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Libro> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody LibroCrearDTO datos) {

        Libro actualizado = libroService.actualizar(id, libroDesdeDTO(datos));

        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        libroService.eliminar(id);

        return ResponseEntity.noContent().build();
    }

    private Libro libroDesdeDTO(LibroCrearDTO datos) {

        Libro libro = new Libro();

        libro.setTitulo(datos.titulo());
        libro.setAutor(datos.autor());
        libro.setIsbn(datos.isbn());
        libro.setGenero(datos.genero());
        libro.setDescripcion(datos.descripcion());
        libro.setCantidadPaginas(datos.cantidadPaginas());
        libro.setEditorial(datos.editorial());
        libro.setPortadaUrl(datos.portadaUrl());

        String fecha = datos.fechaPublicacion();

        if (fecha != null && !fecha.isBlank()) {
            if (fecha.matches("\\d{4}-\\d{2}-\\d{2}")) {
                try {
                    libro.setFechaPublicacion(
                        LocalDate.parse(fecha)
                    );
                } catch (DateTimeParseException e) {
                    throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Fecha de publicacion invalida"
                    );
                }
            } else if (!fecha.matches("\\d{4}")) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usar AAAA o AAAA-MM-DD"
                );
            }
        }

        return libro;
    }
}
