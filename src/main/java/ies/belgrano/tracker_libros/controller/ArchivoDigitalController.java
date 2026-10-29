
package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.ArchivoDigitalRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Usuario;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;
import ies.belgrano.tracker_libros.service.ArchivoDigitalService;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/lecturas")
public class ArchivoDigitalController {

    private final ArchivoDigitalService archivoService;
    private final UsuarioRepository usuarioRepository;

    public ArchivoDigitalController(
            ArchivoDigitalService archivoService,
            UsuarioRepository usuarioRepository) {

        this.archivoService = archivoService;
        this.usuarioRepository = usuarioRepository;
    }

    @PostMapping(
            value = "/{id}/archivo",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ArchivoDigitalRespuestaDTO> subirArchivo(
            @PathVariable Long id,
            @RequestParam("archivo") MultipartFile archivo,
            Authentication authentication) {

        String email = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        ArchivoDigitalRespuestaDTO resultado =
                archivoService.subirArchivo(
                        id,
                        usuario.getId(),
                        archivo);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resultado);
    }
}
