package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.ComunidadDTO.*;
import ies.belgrano.tracker_libros.entity.*;
import ies.belgrano.tracker_libros.enums.EstadoAmistad;
import ies.belgrano.tracker_libros.enums.EstadoClubMiembro;
import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ComunidadService {
    private final UsuarioRepository usuarios;
    private final AmistadRepository amistades;
    private final ClubRepository clubes;
    private final ClubMiembroRepository miembros;
    private final MensajeComunidadRepository mensajes;
    private final CalificacionCompartidaRepository calificaciones;
    private final LecturaRepository lecturas;
    private final ComentarioClubRepository comentarios;

    public ComunidadService(UsuarioRepository usuarios, AmistadRepository amistades,
            ClubRepository clubes, ClubMiembroRepository miembros,
            MensajeComunidadRepository mensajes,
            CalificacionCompartidaRepository calificaciones,
            ComentarioClubRepository comentarios, LecturaRepository lecturas) {
        this.usuarios = usuarios;
        this.amistades = amistades;
        this.clubes = clubes;
        this.miembros = miembros;
        this.mensajes = mensajes;
        this.calificaciones = calificaciones;
        this.comentarios = comentarios;
        this.lecturas = lecturas;
    }

    @Transactional(readOnly = true)
    public List<Amigo> amigos(Long usuarioId) {
        return amistades.listarDe(usuarioId, EstadoAmistad.ACEPTADA).stream()
                .map(a -> amigo(a, usuarioId)).toList();
    }

    @Transactional(readOnly = true)
    public List<Solicitud> solicitudes(Long usuarioId) {
        return amistades.listarDe(usuarioId, EstadoAmistad.PENDIENTE).stream()
                .map(a -> solicitud(a, usuarioId)).toList();
    }

    @Transactional
    public Solicitud solicitarAmistad(Long usuarioId, String email) {
        Usuario actual = usuario(usuarioId);
        Usuario destino = usuarios.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No encontramos ese usuario"));
        if (actual.getId().equals(destino.getId())) {
            throw error(HttpStatus.BAD_REQUEST, "No puedes agregarte como amigo");
        }
        Amistad amistad = amistades.entre(usuarioId, destino.getId()).orElse(null);
        if (amistad != null) {
            if (amistad.estado == EstadoAmistad.ACEPTADA) {
                throw error(HttpStatus.CONFLICT, "Ya son amigos");
            }
            if (amistad.estado == EstadoAmistad.PENDIENTE) {
                throw error(HttpStatus.CONFLICT, "Ya existe una solicitud entre ustedes");
            }
            amistad.solicitante = actual;
            amistad.destinatario = destino;
            amistad.estado = EstadoAmistad.PENDIENTE;
        } else {
            amistad = new Amistad();
            amistad.solicitante = actual;
            amistad.destinatario = destino;
            amistad.estado = EstadoAmistad.PENDIENTE;
        }
        return solicitud(amistades.save(amistad), usuarioId);
    }

    @Transactional
    public void responderSolicitud(Long usuarioId, Long solicitudId, boolean aceptar) {
        Amistad amistad = amistades.findById(solicitudId)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Solicitud no encontrada"));
        if (!amistad.destinatario.getId().equals(usuarioId)
                || amistad.estado != EstadoAmistad.PENDIENTE) {
            throw error(HttpStatus.FORBIDDEN, "No puedes responder esta solicitud");
        }
        if (aceptar) amistad.estado = EstadoAmistad.ACEPTADA;
        else amistades.delete(amistad);
    }

    @Transactional
    public void quitarAmigo(Long usuarioId, Long amigoId) {
        Amistad amistad = amistades.entre(usuarioId, amigoId)
                .filter(a -> a.estado == EstadoAmistad.ACEPTADA)
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Amistad no encontrada"));
        amistades.delete(amistad);
    }

    @Transactional
    public ClubRespuesta crearClub(Long usuarioId, ClubCrear datos) {
        Usuario creador = usuario(usuarioId);
        Club club = new Club();
        club.nombre = datos.nombre().trim();
        club.descripcion = datos.descripcion() == null ? null : datos.descripcion().trim();
        club.esPublico = Boolean.TRUE.equals(datos.publico());
        club.bookKey = textoOpcional(datos.bookKey());
        club.tituloLibro = textoOpcional(datos.tituloLibro());
        club.creador = creador;
        club = clubes.save(club);
        ClubMiembro miembro = new ClubMiembro();
        miembro.club = club;
        miembro.usuario = creador;
        miembro.estado = EstadoClubMiembro.ACTIVO;
        miembro.rol = "ADMIN";
        miembros.save(miembro);
        return club(club, miembro);
    }

    @Transactional(readOnly = true)
    public List<ClubRespuesta> clubes(Long usuarioId) {
        return miembros.findByUsuarioId(usuarioId).stream()
                .map(m -> club(m.club, m)).toList();
    }

    @Transactional(readOnly = true)
    public List<ClubRespuesta> clubesPublicos(Long usuarioId) {
        return clubes.findByEsPublicoTrueOrderByNombreAsc().stream().map(c -> {
            ClubMiembro miembro = miembros.findByClubIdAndUsuarioId(c.id, usuarioId).orElse(null);
            return miembro == null
                    ? respuestaClub(c, "MIEMBRO", "DISPONIBLE")
                    : club(c, miembro);
        }).toList();
    }

    @Transactional
    public ClubRespuesta unirseAClubPublico(Long usuarioId, Long clubId) {
        Club club = club(clubId);
        if (!club.esPublico) throw error(HttpStatus.FORBIDDEN, "Este club es privado");
        ClubMiembro miembro = miembros.findByClubIdAndUsuarioId(clubId, usuarioId).orElse(null);
        if (miembro == null) {
            miembro = new ClubMiembro();
            miembro.club = club;
            miembro.usuario = usuario(usuarioId);
        }
        miembro.estado = EstadoClubMiembro.ACTIVO;
        miembro.rol = "MIEMBRO";
        return club(club, miembros.save(miembro));
    }

    @Transactional
    public ClubRespuesta invitarAlClub(Long usuarioId, Long clubId, String email) {
        Club club = club(clubId);
        ClubMiembro invitador = miembro(clubId, usuarioId);
        if (!"ADMIN".equals(invitador.rol) || invitador.estado != EstadoClubMiembro.ACTIVO) {
            throw error(HttpStatus.FORBIDDEN, "Solo el administrador puede invitar miembros");
        }
        Usuario destino = usuarios.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "No encontramos ese usuario"));
        if (amistades.entre(usuarioId, destino.getId())
                .filter(a -> a.estado == EstadoAmistad.ACEPTADA).isEmpty()) {
            throw error(HttpStatus.FORBIDDEN, "Solo puedes invitar a un amigo aceptado");
        }
        ClubMiembro invitacion = miembros.findByClubIdAndUsuarioId(clubId, destino.getId()).orElse(null);
        if (invitacion != null && invitacion.estado == EstadoClubMiembro.ACTIVO) {
            throw error(HttpStatus.CONFLICT, "Ese usuario ya participa en el club");
        }
        if (invitacion == null) invitacion = new ClubMiembro();
        invitacion.club = club;
        invitacion.usuario = destino;
        invitacion.estado = EstadoClubMiembro.INVITADO;
        invitacion.rol = "MIEMBRO";
        return club(club, miembros.save(invitacion));
    }

    @Transactional
    public void responderInvitacion(Long usuarioId, Long clubId, boolean aceptar) {
        ClubMiembro invitacion = miembro(clubId, usuarioId);
        if (invitacion.estado != EstadoClubMiembro.INVITADO) {
            throw error(HttpStatus.CONFLICT, "No hay una invitacion pendiente");
        }
        if (aceptar) invitacion.estado = EstadoClubMiembro.ACTIVO;
        else miembros.delete(invitacion);
    }

    @Transactional
    public void eliminarClub(Long usuarioId, Long clubId) {
        club(clubId);
        ClubMiembro miembro = miembro(clubId, usuarioId);
        if (!"ADMIN".equals(miembro.rol) || miembro.estado != EstadoClubMiembro.ACTIVO) {
            throw error(HttpStatus.FORBIDDEN, "Solo el administrador puede eliminar el club");
        }
        comentarios.limpiarTemasDelClub(clubId);
        comentarios.borrarDelClub(clubId);
        mensajes.borrarDelClub(clubId);
        miembros.borrarDelClub(clubId);
        clubes.deleteById(clubId);
    }

    @Transactional(readOnly = true)
    public List<MiembroClub> miembros(Long usuarioId, Long clubId) {
        exigirMiembroActivo(clubId, usuarioId);
        return miembros.findByClubIdAndEstadoOrderByFechaUnionAsc(clubId, EstadoClubMiembro.ACTIVO)
                .stream().map(m -> new MiembroClub(m.usuario.getId(), m.usuario.getNombre(),
                        m.usuario.getEmail(), m.estado.name(), m.rol)).toList();
    }

    @Transactional(readOnly = true)
    public List<Mensaje> mensajesClub(Long usuarioId, Long clubId, Long despuesDe) {
        exigirMiembroActivo(clubId, usuarioId);
        return mensajes.nuevosDelClub(clubId, Math.max(0, despuesDe)).stream()
                .map(this::mensaje).toList();
    }

    @Transactional
    public Mensaje enviarMensajeClub(Long usuarioId, Long clubId, String texto) {
        exigirMiembroActivo(clubId, usuarioId);
        MensajeComunidad mensaje = new MensajeComunidad();
        mensaje.autor = usuario(usuarioId);
        mensaje.club = club(clubId);
        mensaje.texto = texto.trim();
        return mensaje(mensajes.save(mensaje));
    }

    @Transactional(readOnly = true)
    public List<Mensaje> mensajesAmigo(Long usuarioId, Long amigoId, Long despuesDe) {
        exigirAmigos(usuarioId, amigoId);
        return mensajes.nuevosEntre(usuarioId, amigoId, Math.max(0, despuesDe)).stream()
                .map(this::mensaje).toList();
    }

    @Transactional
    public Mensaje enviarMensajeAmigo(Long usuarioId, Long amigoId, String texto) {
        exigirAmigos(usuarioId, amigoId);
        MensajeComunidad mensaje = new MensajeComunidad();
        mensaje.autor = usuario(usuarioId);
        mensaje.destinatario = usuario(amigoId);
        mensaje.texto = texto.trim();
        return mensaje(mensajes.save(mensaje));
    }

    @Transactional
    public void sincronizarCalificaciones(Long usuarioId, List<CalificacionCompartir> datos) {
        Usuario usuario = usuario(usuarioId);
        for (CalificacionCompartir dato : datos) {
            CalificacionCompartida item = calificaciones.findByUsuarioIdAndBookKey(usuarioId, dato.bookKey())
                    .orElseGet(CalificacionCompartida::new);
            item.usuario = usuario;
            item.bookKey = dato.bookKey().trim();
            item.titulo = dato.titulo().trim();
            item.autor = dato.autor().trim();
            item.portadaUrl = dato.portadaUrl();
            item.estrellas = dato.estrellas();
            item.comentario = dato.comentario() == null || dato.comentario().isBlank()
                    ? null : dato.comentario().trim();
            calificaciones.save(item);
        }
    }

    @Transactional(readOnly = true)
    public List<Recomendacion> recomendaciones(Long usuarioId) {
        List<Long> amigos = amistades.idsAmigos(usuarioId, EstadoAmistad.ACEPTADA);
        if (amigos.isEmpty()) return List.of();
        return calificaciones.findTop100ByUsuarioIdInAndEstrellasGreaterThanEqualOrderByEstrellasDescFechaActualizacionDesc(amigos, 4)
                .stream().map(r -> new Recomendacion(r.id, r.bookKey, r.titulo, r.autor,
                        r.portadaUrl, r.estrellas, r.comentario, r.usuario.getId(),
                        r.usuario.getNombre(), r.fechaActualizacion)).toList();
    }

    @Transactional(readOnly = true)
    public List<ActividadLectura> actividadAmigos(Long usuarioId) {
        List<Long> amigos = amistades.idsAmigos(usuarioId, EstadoAmistad.ACEPTADA);
        if (amigos.isEmpty()) return List.of();
        return lecturas.findByUsuario_IdInAndEstado(amigos, EstadoLectura.LEYENDO).stream()
                .map(lectura -> new ActividadLectura(lectura.getUsuario().getId(),
                        lectura.getUsuario().getNombre(), lectura.getLibro().getId(),
                        lectura.getLibro().getTitulo(), lectura.getLibro().getAutor(),
                        lectura.getLibro().getPortadaUrl(), lectura.getPorcentajeProgreso(),
                        lectura.getFechaInicio()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ComentariosPagina actividadComentarios(int pagina) {
        int paginaSegura = Math.max(0, pagina);
        Page<CalificacionCompartida> resultado = calificaciones
                .comentariosPublicosRecientes(PageRequest.of(paginaSegura, 5));
        return new ComentariosPagina(resultado.getContent().stream().map(this::recomendacion).toList(),
                resultado.hasNext(), paginaSegura);
    }

    @Transactional(readOnly = true)
    public ComentariosPagina comentariosLibro(String bookKey, int pagina) {
        int paginaSegura = Math.max(0, pagina);
        Page<CalificacionCompartida> resultado = calificaciones
                .comentariosPublicosPorLibro(bookKey, PageRequest.of(paginaSegura, 5));
        return new ComentariosPagina(resultado.getContent().stream().map(this::recomendacion).toList(),
                resultado.hasNext(), paginaSegura);
    }

    @Transactional
    public void eliminarCalificacionCompartida(Long usuarioId, String bookKey) {
        calificaciones.deleteByUsuarioIdAndBookKey(usuarioId, bookKey);
    }

    @Transactional(readOnly = true)
    public List<Comentario> comentarios(Long usuarioId, Long clubId, String bookKey, double hasta) {
        exigirMiembroActivo(clubId, usuarioId);
        return comentarios.findTop200ByClubIdAndBookKeyAndPorcentajePosicionLessThanEqualOrderByPorcentajePosicionAscFechaCreacionAsc(
                        clubId, bookKey, Math.max(0, Math.min(100, hasta)))
                .stream().map(this::comentario).toList();
    }

    @Transactional
    public Comentario comentar(Long usuarioId, Long clubId, String bookKey,
                               String titulo, ComentarioCrear datos) {
        exigirMiembroActivo(clubId, usuarioId);
        ComentarioClub comentario = new ComentarioClub();
        comentario.club = club(clubId);
        comentario.usuario = usuario(usuarioId);
        comentario.bookKey = bookKey;
        comentario.texto = datos.texto().trim();
        if (datos.temaId() != null) {
            ComentarioClub tema = comentarios.findById(datos.temaId())
                    .orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Tema no encontrado"));
            if (tema.tema != null || !tema.club.id.equals(clubId) || !tema.bookKey.equals(bookKey)) {
                throw error(HttpStatus.BAD_REQUEST, "El tema no pertenece a este club y libro");
            }
            comentario.tema = tema;
            comentario.tituloLibro = tema.tituloLibro;
            comentario.porcentajePosicion = tema.porcentajePosicion;
            comentario.ubicacion = tema.ubicacion;
        } else {
            comentario.tituloLibro = titulo;
            comentario.porcentajePosicion = datos.porcentaje();
            comentario.ubicacion = datos.ubicacion();
        }
        return comentario(comentarios.save(comentario));
    }

    private Usuario usuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }
    private Club club(Long id) {
        return clubes.findById(id).orElseThrow(() -> error(HttpStatus.NOT_FOUND, "Club no encontrado"));
    }
    private ClubMiembro miembro(Long clubId, Long usuarioId) {
        return miembros.findByClubIdAndUsuarioId(clubId, usuarioId)
                .orElseThrow(() -> error(HttpStatus.FORBIDDEN, "No perteneces a este club"));
    }
    private void exigirMiembroActivo(Long clubId, Long usuarioId) {
        if (!miembros.existsByClubIdAndUsuarioIdAndEstado(clubId, usuarioId, EstadoClubMiembro.ACTIVO))
            throw error(HttpStatus.FORBIDDEN, "No participas en este club");
    }
    private void exigirAmigos(Long uno, Long dos) {
        if (amistades.entre(uno, dos).filter(a -> a.estado == EstadoAmistad.ACEPTADA).isEmpty())
            throw error(HttpStatus.FORBIDDEN, "Solo puedes conversar con amigos aceptados");
    }
    private Amigo amigo(Amistad a, Long actual) {
        Usuario otro = a.solicitante.getId().equals(actual) ? a.destinatario : a.solicitante;
        return new Amigo(otro.getId(), a.id, otro.getNombre(), otro.getEmail());
    }
    private Solicitud solicitud(Amistad a, Long actual) {
        boolean recibida = a.destinatario.getId().equals(actual);
        Usuario otro = recibida ? a.solicitante : a.destinatario;
        return new Solicitud(a.id, otro.getId(), otro.getNombre(), otro.getEmail(), recibida, a.fechaCreacion);
    }
    private ClubRespuesta club(Club c, ClubMiembro m) {
        return respuestaClub(c, m.rol, m.estado.name());
    }
    private ClubRespuesta respuestaClub(Club c, String rol, String estado) {
        return new ClubRespuesta(c.id, c.nombre, c.descripcion, rol, estado,
                c.creador == null ? "Creador anterior" : c.creador.getNombre(),
                c.esPublico, c.bookKey, c.tituloLibro);
    }
    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
    private Mensaje mensaje(MensajeComunidad m) {
        return new Mensaje(m.id, m.autor.getId(), m.autor.getNombre(), m.texto, m.fechaEnvio);
    }
    private Recomendacion recomendacion(CalificacionCompartida r) {
        return new Recomendacion(r.id, r.bookKey, r.titulo, r.autor, r.portadaUrl,
                r.estrellas, r.comentario, r.usuario.getId(), r.usuario.getNombre(), r.fechaActualizacion);
    }
    private Comentario comentario(ComentarioClub c) {
        return new Comentario(c.id, c.tema == null ? c.id : c.tema.id,
                c.bookKey, c.tituloLibro, c.texto, c.porcentajePosicion,
                c.ubicacion, c.usuario.getId(), c.usuario.getNombre(), c.fechaCreacion);
    }
    private ResponseStatusException error(HttpStatus estado, String texto) {
        return new ResponseStatusException(estado, texto);
    }
}
