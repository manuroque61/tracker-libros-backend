package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.ComentarioClub;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioClubRepository extends JpaRepository<ComentarioClub, Long> {
    List<ComentarioClub> findTop200ByClubIdAndBookKeyAndPorcentajePosicionLessThanEqualOrderByPorcentajePosicionAscFechaCreacionAsc(
            Long clubId, String bookKey, Double hasta);
}
