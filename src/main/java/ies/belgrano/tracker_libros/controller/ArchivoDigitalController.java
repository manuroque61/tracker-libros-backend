
package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.ArchivoDigitalRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Usuario;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;
import ies.belgrano.tracker_libros.service.ArchivoDigitalService;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;

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

        // 1. Subir un archivo PDF o EPUB
        @PostMapping(value = "/{id}/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ArchivoDigitalRespuestaDTO> subirArchivo(
                        @PathVariable Long id,
                        @RequestParam("archivo") MultipartFile archivo,
                        Authentication authentication) {

                Long usuarioId = obtenerUsuarioId(authentication);

                ArchivoDigitalRespuestaDTO resultado = archivoService.subirArchivo(
                                id,
                                usuarioId,
                                archivo);

                return ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(resultado);
        }

        // 2. Consultar los metadatos de un archivo
        @GetMapping("/{id}/archivo/info")
        public ArchivoDigitalRespuestaDTO obtenerInformacion(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long usuarioId = obtenerUsuarioId(authentication);

                return archivoService.obtenerInformacion(
                                id,
                                usuarioId);
        }

        // 3. Recuperar el contenido de un PDF o EPUB
        @GetMapping("/{id}/archivo")
        public ResponseEntity<Resource> obtenerArchivo(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long usuarioId = obtenerUsuarioId(authentication);

                ArchivoDigitalRespuestaDTO info = archivoService.obtenerInformacion(
                                id,
                                usuarioId);

                Resource recurso = archivoService.obtenerContenido(
                                id,
                                usuarioId);

                MediaType tipo = MediaType.parseMediaType(
                                info.tipoContenido());

                ContentDisposition disposicion = ContentDisposition
                                .inline()
                                .filename(
                                                info.nombreOriginal(),
                                                StandardCharsets.UTF_8)
                                .build();

                return ResponseEntity.ok()
                                .contentType(tipo)
                                .contentLength(info.tamanoBytes())
                                .header(
                                                HttpHeaders.CONTENT_DISPOSITION,
                                                disposicion.toString())
                                .header(
                                                "X-Content-Type-Options",
                                                "nosniff")
                                .body(recurso);
        }

        // Método auxiliar para identificar al usuario autenticado
        private Long obtenerUsuarioId(Authentication authentication) {

                String email = authentication.getName();

                Usuario usuario = usuarioRepository.findByEmail(email)
                                .orElseThrow(() -> new ResponseStatusException(
                                                HttpStatus.NOT_FOUND,
                                                "Usuario no encontrado"));

                return usuario.getId();
        }

        @DeleteMapping("/{id}/archivo")
        public ResponseEntity<Void> eliminarArchivo(
                        @PathVariable Long id,
                        Authentication authentication) {

                Long usuarioId = obtenerUsuarioId(authentication);

                archivoService.eliminarArchivo(
                                id,
                                usuarioId);

                return ResponseEntity.noContent().build();
        }

}
