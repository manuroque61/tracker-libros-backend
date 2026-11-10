package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.ClubMiembro;
import ies.belgrano.tracker_libros.enums.EstadoClubMiembro;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubMiembroRepository extends JpaRepository<ClubMiembro, Long> {
    @Query("select m from ClubMiembro m join fetch m.club c join fetch c.creador "
            + "where m.usuario.id = :usuarioId")
    List<ClubMiembro> findByUsuarioId(@Param("usuarioId") Long usuarioId);
    List<ClubMiembro> findByClubIdAndEstadoOrderByFechaUnionAsc(Long clubId, EstadoClubMiembro estado);
    Optional<ClubMiembro> findByClubIdAndUsuarioId(Long clubId, Long usuarioId);
    boolean existsByClubIdAndUsuarioIdAndEstado(Long clubId, Long usuarioId, EstadoClubMiembro estado);

    @Modifying(clearAutomatically = true)
    @Query("delete from ClubMiembro m where m.club.id = :clubId")
    void borrarDelClub(@Param("clubId") Long clubId);
}
