package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.ComentarioClub;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ComentarioClubRepository extends JpaRepository<ComentarioClub, Long> {
    List<ComentarioClub> findTop200ByClubIdAndBookKeyAndPorcentajePosicionLessThanEqualOrderByPorcentajePosicionAscFechaCreacionAsc(
            Long clubId, String bookKey, Double hasta);

    @Modifying(clearAutomatically = true)
    @Query("update ComentarioClub c set c.tema = null where c.club.id = :clubId")
    void limpiarTemasDelClub(@Param("clubId") Long clubId);

    @Modifying(clearAutomatically = true)
    @Query("delete from ComentarioClub c where c.club.id = :clubId")
    void borrarDelClub(@Param("clubId") Long clubId);
}
