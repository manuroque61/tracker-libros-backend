package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.Amistad;
import ies.belgrano.tracker_libros.enums.EstadoAmistad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AmistadRepository extends JpaRepository<Amistad, Long> {
    @Query("select a from Amistad a where "
            + "(a.solicitante.id = :uno and a.destinatario.id = :dos) or "
            + "(a.solicitante.id = :dos and a.destinatario.id = :uno)")
    Optional<Amistad> entre(@Param("uno") Long uno, @Param("dos") Long dos);

    @Query("select a from Amistad a where a.estado = :estado and "
            + "(a.solicitante.id = :usuarioId or a.destinatario.id = :usuarioId) "
            + "order by a.fechaCreacion desc")
    List<Amistad> listarDe(@Param("usuarioId") Long usuarioId,
                           @Param("estado") EstadoAmistad estado);

    @Query("select a from Amistad a where a.estado = :estado and a.destinatario.id = :usuarioId "
            + "order by a.fechaCreacion desc")
    List<Amistad> solicitudesRecibidas(@Param("usuarioId") Long usuarioId,
                                      @Param("estado") EstadoAmistad estado);

    @Query("select case when a.solicitante.id = :usuarioId then a.destinatario.id "
            + "else a.solicitante.id end from Amistad a where a.estado = :estado and "
            + "(a.solicitante.id = :usuarioId or a.destinatario.id = :usuarioId)")
    List<Long> idsAmigos(@Param("usuarioId") Long usuarioId,
                         @Param("estado") EstadoAmistad estado);
}
