package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.GutenbergLibroDTO;
import ies.belgrano.tracker_libros.service.GutenbergService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gutenberg")
public class GutenbergController {

    private final GutenbergService gutenbergService;

    public GutenbergController(GutenbergService gutenbergService) {
        this.gutenbergService = gutenbergService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<GutenbergLibroDTO>> buscar(
            @RequestParam(defaultValue = "es") String idioma,
            @RequestParam(required = false) String busqueda) {

        List<GutenbergLibroDTO> libros = gutenbergService.buscarLibros(idioma, busqueda);
        return ResponseEntity.ok(libros);
    }
}
