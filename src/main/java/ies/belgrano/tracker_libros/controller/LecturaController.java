package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.LecturaRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Usuario;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;
import ies.belgrano.tracker_libros.service.LecturaService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ies.belgrano.tracker_libros.dto.RegistroLecturaDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import ies.belgrano.tracker_libros.dto.ActualizarProgresoDTO;
import ies.belgrano.tracker_libros.dto.ActualizarEstadoDTO;

import java.util.List;

@RestController
@RequestMapping("/api/lecturas")
public class LecturaController {

    private final LecturaService lecturaService;
    private final UsuarioRepository usuarioRepository;

    public LecturaController(
            LecturaService lecturaService,
            UsuarioRepository usuarioRepository) {

        this.lecturaService = lecturaService;
        this.usuarioRepository = usuarioRepository;
    }

    // Consultar mi biblioteca
    @GetMapping
    public List<LecturaRespuestaDTO> obtenerMiBiblioteca(
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return lecturaService.obtenerBiblioteca(usuarioId);
    }

    // Consultar una lectura específica
    @GetMapping("/{id}")
    public LecturaRespuestaDTO obtenerLectura(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return lecturaService.obtenerDetalle(id, usuarioId);
    }

    @PostMapping
    public ResponseEntity<LecturaRespuestaDTO> agregarLibro(
            @Valid @RequestBody RegistroLecturaDTO datos,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        LecturaRespuestaDTO lectura = lecturaService.registrarEnBiblioteca(
                usuarioId,
                datos.libroId(),
                datos.formato());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lectura);
    }

    @PatchMapping("/{id}/progreso")
    public LecturaRespuestaDTO actualizarProgreso(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarProgresoDTO datos,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return lecturaService.actualizarProgresoEnBiblioteca(
                id,
                usuarioId,
                datos.paginaActual(),
                datos.porcentajeDigital(),
                datos.ubicacionDigital());
    }

    @PatchMapping("/{id}/estado")
    public LecturaRespuestaDTO cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoDTO datos,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        return lecturaService.cambiarEstado(
                id,
                usuarioId,
                datos.estado());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLectura(
            @PathVariable Long id,
            Authentication authentication) {

        Long usuarioId = obtenerUsuarioId(authentication);

        lecturaService.eliminarDeBiblioteca(id, usuarioId);

        return ResponseEntity.noContent().build();
    }

    private Long obtenerUsuarioId(Authentication authentication) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        return usuario.getId();
    }
}