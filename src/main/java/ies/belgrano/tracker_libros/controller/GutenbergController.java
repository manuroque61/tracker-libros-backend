package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.GutenbergPaginaDTO;
import ies.belgrano.tracker_libros.service.GutenbergService;
import ies.belgrano.tracker_libros.service.RateLimitService;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gutenberg")
public class GutenbergController {

    private static final int MAX_PETICIONES = 60;
    private static final Duration VENTANA = Duration.ofMinutes(1);

    private final GutenbergService gutenbergService;
    private final RateLimitService rateLimitService;

    public GutenbergController(
            GutenbergService gutenbergService,
            RateLimitService rateLimitService) {
        this.gutenbergService = gutenbergService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/buscar")
    public ResponseEntity<GutenbergPaginaDTO> buscar(
            @RequestParam(defaultValue = "es") String idioma,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "1") int pagina,
            HttpServletRequest request) {

        String clave = "gutenberg|" + request.getRemoteAddr();
        rateLimitService.verificar(clave, MAX_PETICIONES);
        rateLimitService.contar(clave, VENTANA);

        return ResponseEntity.ok(gutenbergService.buscarPagina(idioma, busqueda, pagina));
    }
}
