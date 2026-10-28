package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.AnotacionCrearDTO;
import ies.belgrano.tracker_libros.dto.AnotacionRespuestaDTO;
import ies.belgrano.tracker_libros.service.AnotacionService;
import ies.belgrano.tracker_libros.service.UsuarioActualService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros/anotaciones")
public class AnotacionController {

    private final AnotacionService anotacionService;
    private final UsuarioActualService usuarioActualService;

    public AnotacionController(
            AnotacionService anotacionService,
            UsuarioActualService usuarioActualService) {

        this.anotacionService = anotacionService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping("/{libroId}")
    public List<AnotacionRespuestaDTO> listar(
            @PathVariable Long libroId,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return anotacionService.listarPorLibro(usuarioId, libroId);
    }

    @PostMapping("/{libroId}")
    public ResponseEntity<AnotacionRespuestaDTO> crear(
            @PathVariable Long libroId,
            @Valid @RequestBody AnotacionCrearDTO datos,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(anotacionService.crear(usuarioId, datos));
    }

    @DeleteMapping("/{libroId}/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long libroId,
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        anotacionService.eliminar(id, usuarioId);

        return ResponseEntity.noContent().build();
    }
}
