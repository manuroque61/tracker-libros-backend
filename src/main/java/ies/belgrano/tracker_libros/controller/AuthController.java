
package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.LoginRequestDTO;
import ies.belgrano.tracker_libros.dto.LoginResponseDTO;
import ies.belgrano.tracker_libros.dto.RegistroUsuarioDTO;
import ies.belgrano.tracker_libros.dto.UsuarioRespuestaDTO;

import ies.belgrano.tracker_libros.service.JwtService;
import ies.belgrano.tracker_libros.service.RateLimitService;
import ies.belgrano.tracker_libros.service.UsuarioService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Locale;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final int LOGIN_MAX_INTENTOS = 10;
    private static final int LOGIN_MAX_POR_IP = 30;
    private static final Duration LOGIN_VENTANA = Duration.ofMinutes(15);

    private static final int REGISTRO_MAX_POR_IP = 5;
    private static final Duration REGISTRO_VENTANA = Duration.ofHours(1);

    private final UsuarioService usuarioService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RateLimitService rateLimitService;

    public AuthController(
            UsuarioService usuarioService,
            JwtService jwtService,
            AuthenticationManager authenticationManager,
            RateLimitService rateLimitService) {

        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/registro")
    public ResponseEntity<UsuarioRespuestaDTO> registrar(
            @Valid @RequestBody RegistroUsuarioDTO datos,
            HttpServletRequest request) {

        String clave = "registro|" + ipCliente(request);
        rateLimitService.verificar(clave, REGISTRO_MAX_POR_IP);
        rateLimitService.contar(clave, REGISTRO_VENTANA);

        UsuarioRespuestaDTO usuario =
                usuarioService.registrar(datos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuario);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO datos,
            HttpServletRequest request) {

        String email = datos.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String ip = ipCliente(request);
        String claveCuenta = "login|" + ip + "|" + email;
        String claveIp = "login-ip|" + ip;

        rateLimitService.verificar(claveCuenta, LOGIN_MAX_INTENTOS);
        rateLimitService.verificar(claveIp, LOGIN_MAX_POR_IP);

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    email,
                    datos.password()
                )
            );
        } catch (AuthenticationException e) {
            rateLimitService.contar(claveCuenta, LOGIN_VENTANA);
            rateLimitService.contar(claveIp, LOGIN_VENTANA);

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Credenciales invalidas"
            );
        }

        rateLimitService.limpiar(claveCuenta);

        String token = jwtService.generarToken(email);

        LoginResponseDTO respuesta = new LoginResponseDTO(
            token,
            "Bearer",
            jwtService.obtenerDuracionSegundos()
        );

        return ResponseEntity.ok(respuesta);
    }

    private String ipCliente(HttpServletRequest request) {
        return request.getRemoteAddr();
    }
}
