
package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.AgregarLibroBibliotecaDTO;
import ies.belgrano.tracker_libros.dto.LecturaRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Usuario;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;
import ies.belgrano.tracker_libros.service.BibliotecaAltaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/biblioteca")
public class BibliotecaController {

    private final BibliotecaAltaService bibliotecaAltaService;
    private final UsuarioRepository usuarioRepository;

    public BibliotecaController(
            BibliotecaAltaService bibliotecaAltaService,
            UsuarioRepository usuarioRepository) {

        this.bibliotecaAltaService = bibliotecaAltaService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping("/libros")
    public ResponseEntity<LecturaRespuestaDTO> agregarLibro(
            @Valid @RequestBody AgregarLibroBibliotecaDTO datos,
            Authentication authentication) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"
                ));

        LecturaRespuestaDTO lectura =
                bibliotecaAltaService.agregarLibro(
                        usuario.getId(),
                        datos
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(lectura);
    }
}
