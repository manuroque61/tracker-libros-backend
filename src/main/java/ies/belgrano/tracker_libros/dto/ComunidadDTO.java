package ies.belgrano.tracker_libros.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ComunidadDTO {
    private ComunidadDTO() {}

    public record Amigo(Long usuarioId, Long solicitudId, String nombre, String email) {}
    public record Solicitud(Long id, Long usuarioId, String nombre, String email,
                            boolean recibida, LocalDateTime creadaEn) {}
    public record SolicitudCrear(@NotBlank @Email String email) {}
    public record ResponderSolicitud(@NotNull Boolean aceptar) {}

    public record ClubCrear(@NotBlank @Size(max = 100) String nombre,
                            @Size(max = 500) String descripcion, Boolean publico,
                            @Size(max = 512) String bookKey,
                            @Size(max = 200) String tituloLibro) {}
    public record InvitarClub(@NotBlank @Email String email) {}
    public record ResponderInvitacion(@NotNull Boolean aceptar) {}
    public record MiembroClub(Long usuarioId, String nombre, String email,
                              String estado, String rol) {}
    public record ClubRespuesta(Long id, String nombre, String descripcion,
                                String rol, String estado, String nombreCreador,
                                boolean publico, String bookKey, String tituloLibro) {}

    public record MensajeCrear(@NotBlank @Size(max = 2000) String texto) {}
    public record Mensaje(Long id, Long autorId, String autor, String texto,
                          LocalDateTime enviadoEn) {}

    public record CalificacionCompartir(
            @NotBlank @Size(max = 512) String bookKey,
            @NotBlank @Size(max = 200) String titulo,
            @NotBlank @Size(max = 200) String autor,
            @Size(max = 1000) String portadaUrl,
            @NotNull @Min(1) @Max(5) Integer estrellas,
            @Size(max = 1000) String comentario) {}
    public record Recomendacion(Long id, String bookKey, String titulo, String autor,
                                String portadaUrl, Integer estrellas, String comentario,
                                Long amigoId, String amigo, LocalDateTime actualizadoEn) {}
    public record ActividadLectura(Long amigoId, String amigo, Long libroId, String titulo,
                                   String autor, String portadaUrl, Double porcentaje, LocalDate fechaInicio) {}
    public record ComentariosPagina(List<Recomendacion> comentarios, boolean hayMas, int pagina) {}

    public record ComentarioCrear(@NotBlank @Size(max = 2000) String texto,
                                  @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double porcentaje,
                                  @Size(max = 1000) String ubicacion, Long temaId) {}
    public record Comentario(Long id, Long temaId, String bookKey, String tituloLibro, String texto,
                             Double porcentaje, String ubicacion, Long autorId,
                             String autor, LocalDateTime creadoEn) {}

    public record CalificacionesSync(@NotNull List<@Valid CalificacionCompartir> calificaciones) {}
}
