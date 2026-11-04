
package ies.belgrano.tracker_libros.dto;

import ies.belgrano.tracker_libros.enums.Rol;

import java.time.LocalDateTime;

public record UsuarioRespuestaDTO(
    Long id,
    String nombre,
    String email,
    LocalDateTime fechaRegistro,
    Rol rol
) {
}
