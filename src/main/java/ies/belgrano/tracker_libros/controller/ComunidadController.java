package ies.belgrano.tracker_libros.controller;

import ies.belgrano.tracker_libros.dto.ComunidadDTO.*;
import ies.belgrano.tracker_libros.service.ComunidadService;
import ies.belgrano.tracker_libros.service.UsuarioActualService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comunidad")
public class ComunidadController {
    private final ComunidadService comunidad;
    private final UsuarioActualService usuarioActual;

    public ComunidadController(ComunidadService comunidad, UsuarioActualService usuarioActual) {
        this.comunidad = comunidad;
        this.usuarioActual = usuarioActual;
    }

    @GetMapping("/amigos")
    public List<Amigo> amigos(Authentication auth) {
        return comunidad.amigos(usuarioActual.obtenerId(auth));
    }

    @GetMapping("/amigos/solicitudes")
    public List<Solicitud> solicitudes(Authentication auth) {
        return comunidad.solicitudes(usuarioActual.obtenerId(auth));
    }

    @PostMapping("/amigos/solicitudes")
    public ResponseEntity<Solicitud> solicitar(@Valid @RequestBody SolicitudCrear datos, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comunidad.solicitarAmistad(usuarioActual.obtenerId(auth), datos.email()));
    }

    @PatchMapping("/amigos/solicitudes/{id}")
    public ResponseEntity<Void> responder(@PathVariable Long id,
            @Valid @RequestBody ResponderSolicitud datos, Authentication auth) {
        comunidad.responderSolicitud(usuarioActual.obtenerId(auth), id, datos.aceptar());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/amigos/{usuarioId}")
    public ResponseEntity<Void> quitarAmigo(@PathVariable Long usuarioId, Authentication auth) {
        comunidad.quitarAmigo(usuarioActual.obtenerId(auth), usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clubes")
    public List<ClubRespuesta> clubes(Authentication auth) {
        return comunidad.clubes(usuarioActual.obtenerId(auth));
    }

    @GetMapping("/clubes/publicos")
    public List<ClubRespuesta> clubesPublicos(Authentication auth) {
        return comunidad.clubesPublicos(usuarioActual.obtenerId(auth));
    }

    @PostMapping("/clubes/{clubId}/unirse")
    public ClubRespuesta unirseAClub(@PathVariable Long clubId, Authentication auth) {
        return comunidad.unirseAClubPublico(usuarioActual.obtenerId(auth), clubId);
    }

    @PostMapping("/clubes")
    public ResponseEntity<ClubRespuesta> crearClub(@Valid @RequestBody ClubCrear datos, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comunidad.crearClub(usuarioActual.obtenerId(auth), datos));
    }

    @PostMapping("/clubes/{clubId}/invitaciones")
    public ClubRespuesta invitar(@PathVariable Long clubId, @Valid @RequestBody InvitarClub datos,
            Authentication auth) {
        return comunidad.invitarAlClub(usuarioActual.obtenerId(auth), clubId, datos.email());
    }

    @PatchMapping("/clubes/{clubId}/invitacion")
    public ResponseEntity<Void> responderInvitacion(@PathVariable Long clubId,
            @Valid @RequestBody ResponderInvitacion datos, Authentication auth) {
        comunidad.responderInvitacion(usuarioActual.obtenerId(auth), clubId, datos.aceptar());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clubes/{clubId}/miembros")
    public List<MiembroClub> miembros(@PathVariable Long clubId, Authentication auth) {
        return comunidad.miembros(usuarioActual.obtenerId(auth), clubId);
    }

    @GetMapping("/clubes/{clubId}/mensajes")
    public List<Mensaje> mensajesClub(@PathVariable Long clubId,
            @RequestParam(defaultValue = "0") Long despuesDe, Authentication auth) {
        return comunidad.mensajesClub(usuarioActual.obtenerId(auth), clubId, despuesDe);
    }

    @PostMapping("/clubes/{clubId}/mensajes")
    public ResponseEntity<Mensaje> enviarClub(@PathVariable Long clubId,
            @Valid @RequestBody MensajeCrear datos, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comunidad.enviarMensajeClub(usuarioActual.obtenerId(auth), clubId, datos.texto()));
    }

    @GetMapping("/amigos/{amigoId}/mensajes")
    public List<Mensaje> mensajesAmigo(@PathVariable Long amigoId,
            @RequestParam(defaultValue = "0") Long despuesDe, Authentication auth) {
        return comunidad.mensajesAmigo(usuarioActual.obtenerId(auth), amigoId, despuesDe);
    }

    @PostMapping("/amigos/{amigoId}/mensajes")
    public ResponseEntity<Mensaje> enviarAmigo(@PathVariable Long amigoId,
            @Valid @RequestBody MensajeCrear datos, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comunidad.enviarMensajeAmigo(usuarioActual.obtenerId(auth), amigoId, datos.texto()));
    }

    @PostMapping("/calificaciones/sync")
    public ResponseEntity<Void> sincronizar(@Valid @RequestBody CalificacionesSync datos, Authentication auth) {
        comunidad.sincronizarCalificaciones(usuarioActual.obtenerId(auth), datos.calificaciones());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recomendaciones")
    public List<Recomendacion> recomendaciones(Authentication auth) {
        return comunidad.recomendaciones(usuarioActual.obtenerId(auth));
    }

    @GetMapping("/actividad")
    public List<ActividadLectura> actividad(Authentication auth) {
        return comunidad.actividadAmigos(usuarioActual.obtenerId(auth));
    }

    @GetMapping("/actividad/comentarios")
    public ComentariosPagina actividadComentarios(@RequestParam(defaultValue = "0") int pagina) {
        return comunidad.actividadComentarios(pagina);
    }

    @GetMapping("/libros/{bookKey}/comentarios")
    public ComentariosPagina comentariosLibro(@PathVariable String bookKey,
            @RequestParam(defaultValue = "0") int pagina) {
        return comunidad.comentariosLibro(bookKey, pagina);
    }

    @DeleteMapping("/calificaciones/{bookKey}")
    public ResponseEntity<Void> eliminarCalificacion(@PathVariable String bookKey, Authentication auth) {
        comunidad.eliminarCalificacionCompartida(usuarioActual.obtenerId(auth), bookKey);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/clubes/{clubId}/libros/{bookKey}/comentarios")
    public List<Comentario> comentarios(@PathVariable Long clubId, @PathVariable String bookKey,
            @RequestParam(defaultValue = "0") double hasta, Authentication auth) {
        return comunidad.comentarios(usuarioActual.obtenerId(auth), clubId, bookKey, hasta);
    }

    @PostMapping("/clubes/{clubId}/libros/{bookKey}/comentarios")
    public ResponseEntity<Comentario> comentar(@PathVariable Long clubId,
            @PathVariable String bookKey, @RequestParam String titulo,
            @Valid @RequestBody ComentarioCrear datos, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(comunidad.comentar(
                usuarioActual.obtenerId(auth), clubId, bookKey, titulo, datos));
    }
}
