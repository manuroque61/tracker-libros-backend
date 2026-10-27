
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.RegistroUsuarioDTO;
import ies.belgrano.tracker_libros.dto.UsuarioRespuestaDTO;
import ies.belgrano.tracker_libros.entity.Usuario;
import ies.belgrano.tracker_libros.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UsuarioRespuestaDTO registrar(RegistroUsuarioDTO datos) {

        String email = datos.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Ya existe una cuenta con ese email"
            );
        }

        Usuario usuario = new Usuario();

        usuario.setNombre(datos.nombre().trim());
        usuario.setEmail(email);

        usuario.setPasswordHash(
            passwordEncoder.encode(datos.password())
        );

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        return new UsuarioRespuestaDTO(
            guardado.getId(),
            guardado.getNombre(),
            guardado.getEmail(),
            guardado.getFechaRegistro()
        );
    }
}
