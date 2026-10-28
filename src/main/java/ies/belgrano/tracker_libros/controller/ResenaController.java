package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.ResenaGuardarDTO;
import ies.belgrano.tracker_libros.dto.ResenaRespuestaDTO;
import ies.belgrano.tracker_libros.service.ResenaService;
import ies.belgrano.tracker_libros.service.UsuarioActualService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/libros/resenas")
public class ResenaController {

    private final ResenaService resenaService;
    private final UsuarioActualService usuarioActualService;

    public ResenaController(
            ResenaService resenaService,
            UsuarioActualService usuarioActualService) {

        this.resenaService = resenaService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping("/{libroId}")
    public ResponseEntity<ResenaRespuestaDTO> obtener(
            @PathVariable Long libroId,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return ResponseEntity.ok(resenaService.obtener(usuarioId, libroId));
    }

    @GetMapping
    public List<ResenaRespuestaDTO> listar(
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return resenaService.listar(usuarioId);
    }

    @PutMapping("/{libroId}")
    public ResponseEntity<ResenaRespuestaDTO> guardar(
            @PathVariable Long libroId,
            @Valid @RequestBody ResenaGuardarDTO datos,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return ResponseEntity.ok(resenaService.guardar(usuarioId, libroId, datos));
    }

    @DeleteMapping("/{libroId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long libroId,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        resenaService.eliminar(usuarioId, libroId);

        return ResponseEntity.noContent().build();
    }
}
