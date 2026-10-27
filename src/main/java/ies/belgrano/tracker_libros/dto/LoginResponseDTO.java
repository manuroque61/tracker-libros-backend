
package ies.belgrano.tracker_libros.dto;

public record LoginResponseDTO(
    String accessToken,
    String tokenType,
    long expiresIn
) {
}
