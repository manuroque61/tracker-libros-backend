package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.SesionCrearDTO;
import ies.belgrano.tracker_libros.dto.SesionRespuestaDTO;
import ies.belgrano.tracker_libros.service.SesionLecturaService;
import ies.belgrano.tracker_libros.service.UsuarioActualService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturas/sesiones")
public class SesionLecturaController {

    private final SesionLecturaService sesionLecturaService;
    private final UsuarioActualService usuarioActualService;

    public SesionLecturaController(
            SesionLecturaService sesionLecturaService,
            UsuarioActualService usuarioActualService) {

        this.sesionLecturaService = sesionLecturaService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping("/{id}")
    public List<SesionRespuestaDTO> listar(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return sesionLecturaService.listar(id, usuarioId);
    }

    @PostMapping("/{id}")
    public ResponseEntity<SesionRespuestaDTO> registrar(
            @PathVariable Long id,
            @Valid @RequestBody SesionCrearDTO datos,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sesionLecturaService.registrar(id, usuarioId, datos));
    }
}
