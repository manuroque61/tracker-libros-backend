
package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequestDTO(

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no es valido")
    @Size(max = 150)
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(max = 72,
          message = "La contraseña no puede superar 72 caracteres")
    String password

) {
}
