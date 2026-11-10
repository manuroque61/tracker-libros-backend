package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.Club;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ClubRepository extends JpaRepository<Club, Long> {
    @Query("select c from Club c join fetch c.creador where c.esPublico = true order by c.nombre")
    List<Club> findByEsPublicoTrueOrderByNombreAsc();
}
