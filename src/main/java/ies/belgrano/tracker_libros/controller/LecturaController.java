package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.CambioEstadoDTO;
import ies.belgrano.tracker_libros.dto.LecturaRespuestaDTO;
import ies.belgrano.tracker_libros.dto.ProgresoLecturaDTO;
import ies.belgrano.tracker_libros.dto.RegistroLecturaDTO;
import ies.belgrano.tracker_libros.dto.SesionCrearDTO;
import ies.belgrano.tracker_libros.dto.SesionRespuestaDTO;
import ies.belgrano.tracker_libros.service.LecturaService;
import ies.belgrano.tracker_libros.service.SesionLecturaService;
import ies.belgrano.tracker_libros.service.UsuarioActualService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lecturas")
public class LecturaController {

    private final LecturaService lecturaService;
    private final SesionLecturaService sesionLecturaService;
    private final UsuarioActualService usuarioActualService;

    public LecturaController(
            LecturaService lecturaService,
            SesionLecturaService sesionLecturaService,
            UsuarioActualService usuarioActualService) {

        this.lecturaService = lecturaService;
        this.sesionLecturaService = sesionLecturaService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public List<LecturaRespuestaDTO> obtenerMiBiblioteca(
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return lecturaService.obtenerBiblioteca(usuarioId);
    }

    @GetMapping("/{id}")
    public LecturaRespuestaDTO obtenerLectura(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return lecturaService.obtenerDetalle(id, usuarioId);
    }

    @PostMapping
    public ResponseEntity<LecturaRespuestaDTO> agregarLibro(
            @Valid @RequestBody RegistroLecturaDTO datos,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        LecturaRespuestaDTO lectura = lecturaService.registrarEnBiblioteca(
                usuarioId,
                datos.libroId(),
                datos.formato());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lectura);
    }

    @PatchMapping("/{id}/progreso")
    public ResponseEntity<LecturaRespuestaDTO> actualizarProgreso(
            @PathVariable Long id,
            @Valid @RequestBody ProgresoLecturaDTO datos,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        LecturaRespuestaDTO actualizacion = lecturaService
                .actualizarProgresoDTO(id, usuarioId,
                        datos.paginaActual(), datos.porcentaje(), datos.ubicacionDigital());

        return ResponseEntity.ok(actualizacion);
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<LecturaRespuestaDTO> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambioEstadoDTO datos,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return ResponseEntity.ok(
                lecturaService.cambiarEstado(id, usuarioId, datos.estado()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        lecturaService.eliminar(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/sesiones")
    public List<SesionRespuestaDTO> listarSesiones(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return sesionLecturaService.listar(id, usuarioId);
    }

    @PostMapping("/{id}/sesiones")
    public ResponseEntity<SesionRespuestaDTO> registrarSesion(
            @PathVariable Long id,
            @Valid @RequestBody SesionCrearDTO datos,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sesionLecturaService.registrar(id, usuarioId, datos));
    }
}
