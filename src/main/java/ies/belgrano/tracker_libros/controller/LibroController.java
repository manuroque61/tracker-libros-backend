
package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.LibroExternoDTO;
import ies.belgrano.tracker_libros.service.LibroExternoService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/libros")
public class LibroController {

    private final LibroExternoService libroExternoService;

    public LibroController(LibroExternoService libroExternoService) {
        this.libroExternoService = libroExternoService;
    }

    @GetMapping("/buscar-isbn/{isbn}")
    public ResponseEntity<LibroExternoDTO> buscarPorIsbn(
            @PathVariable String isbn) {

        if (!isbn.matches("(?:[0-9]{13}|[0-9]{9}[0-9Xx])")) {
            return ResponseEntity.badRequest().build();
        }

        LibroExternoDTO libro =
                libroExternoService.buscarPorIsbn(isbn);

        return ResponseEntity.ok(libro);
    }
}
