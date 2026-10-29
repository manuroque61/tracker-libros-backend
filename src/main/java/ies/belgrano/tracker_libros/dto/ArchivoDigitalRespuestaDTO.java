
package ies.belgrano.tracker_libros.dto;

import java.time.LocalDateTime;

public record ArchivoDigitalRespuestaDTO(
        Long id,
        Long lecturaId,
        String nombreOriginal,
        String tipoContenido,
        Long tamanoBytes,
        LocalDateTime fechaSubida
) {
}
