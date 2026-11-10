package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.MensajeComunidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MensajeComunidadRepository extends JpaRepository<MensajeComunidad, Long> {
    List<MensajeComunidad> findTop100ByClubIdOrderByIdDesc(Long clubId);

    @Query("select m from MensajeComunidad m where m.club.id = :clubId and m.id > :despuesDe order by m.id asc")
    List<MensajeComunidad> nuevosDelClub(@Param("clubId") Long clubId, @Param("despuesDe") Long despuesDe);

    @Query("select m from MensajeComunidad m where m.club is null and m.id > :despuesDe and "
            + "((m.autor.id = :uno and m.destinatario.id = :dos) or "
            + "(m.autor.id = :dos and m.destinatario.id = :uno)) order by m.id asc")
    List<MensajeComunidad> nuevosEntre(@Param("uno") Long uno, @Param("dos") Long dos,
                                       @Param("despuesDe") Long despuesDe);
}
