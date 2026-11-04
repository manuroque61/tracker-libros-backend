
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.entity.Usuario;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;

import java.time.Duration;
import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final UsuarioRepository usuarioRepository;

    private static final Duration DURACION =
            Duration.ofMinutes(30);

    public JwtService(
            JwtEncoder jwtEncoder,
            UsuarioRepository usuarioRepository) {
        this.jwtEncoder = jwtEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    public String generarToken(String email) {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));

        Instant ahora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("tracker-libros")
                .subject(email)
                .claim("rol", usuario.rolEfectivo().name())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(DURACION))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    public long obtenerDuracionSegundos() {
        return DURACION.toSeconds();
    }
}
