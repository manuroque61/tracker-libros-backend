package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.EstadisticasDTO;
import ies.belgrano.tracker_libros.service.EstadisticasService;
import ies.belgrano.tracker_libros.service.UsuarioActualService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasController {

    private final EstadisticasService estadisticaService;
    private final UsuarioActualService usuarioActualService;

    public EstadisticasController(
            EstadisticasService estadisticaService,
            UsuarioActualService usuarioActualService) {

        this.estadisticaService = estadisticaService;
        this.usuarioActualService = usuarioActualService;
    }

    @GetMapping
    public ResponseEntity<EstadisticasDTO> obtener(
            @RequestParam(required = false) ies.belgrano.tracker_libros.enums.FormatoLectura formato,
            Authentication authentication) {

        Long usuarioId = usuarioActualService.obtenerId(authentication);

        EstadisticasDTO resultado = estadisticaService.obtener(usuarioId, formato);

        return ResponseEntity.ok(resultado);
    }
}
