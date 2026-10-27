
package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.LoginRequestDTO;
import ies.belgrano.tracker_libros.dto.LoginResponseDTO;
import ies.belgrano.tracker_libros.dto.RegistroUsuarioDTO;
import ies.belgrano.tracker_libros.dto.UsuarioRespuestaDTO;

import ies.belgrano.tracker_libros.service.JwtService;
import ies.belgrano.tracker_libros.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            UsuarioService usuarioService,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {

        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioRespuestaDTO> registrar(
            @Valid @RequestBody RegistroUsuarioDTO datos) {

        UsuarioRespuestaDTO usuario =
                usuarioService.registrar(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO datos) {

        String email = datos.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    email,
                    datos.password()
                )
            );
        } catch (AuthenticationException e) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales invalidas"
            );
        }

        String token = jwtService.generarToken(email);

        LoginResponseDTO respuesta = new LoginResponseDTO(
            token,
            "Bearer",
            jwtService.obtenerDuracionSegundos()
        );

        return ResponseEntity.ok(respuesta);
    }
}
