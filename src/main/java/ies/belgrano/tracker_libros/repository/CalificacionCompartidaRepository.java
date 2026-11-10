package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.CalificacionCompartida;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalificacionCompartidaRepository extends JpaRepository<CalificacionCompartida, Long> {
    Optional<CalificacionCompartida> findByUsuarioIdAndBookKey(Long usuarioId, String bookKey);
    List<CalificacionCompartida> findByUsuarioIdInAndBookKeyOrderByFechaActualizacionDesc(
            List<Long> usuarioIds, String bookKey);
    @Query("""
            select c from CalificacionCompartida c
            where c.bookKey = :bookKey and c.comentario is not null and trim(c.comentario) <> ''
            order by c.fechaActualizacion desc
            """)
    Page<CalificacionCompartida> comentariosPublicosPorLibro(
            @Param("bookKey") String bookKey, Pageable pageable);

    @Query("""
            select c from CalificacionCompartida c
            where c.comentario is not null and trim(c.comentario) <> ''
            order by c.fechaActualizacion desc
            """)
    Page<CalificacionCompartida> comentariosPublicosRecientes(Pageable pageable);
    List<CalificacionCompartida> findTop100ByUsuarioIdInAndComentarioIsNotNullOrderByFechaActualizacionDesc(
            List<Long> usuarioIds);
    void deleteByUsuarioIdAndBookKey(Long usuarioId, String bookKey);
    List<CalificacionCompartida> findTop100ByUsuarioIdInAndEstrellasGreaterThanEqualOrderByEstrellasDescFechaActualizacionDesc(
            List<Long> usuarioIds, Integer estrellas);
}
