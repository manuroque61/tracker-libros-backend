
package ies.belgrano.tracker_libros.dto;

import java.time.LocalDateTime;

public record UsuarioRespuestaDTO(
    Long id,
    String nombre,
    String email,
    LocalDateTime fechaRegistro
) {
}
