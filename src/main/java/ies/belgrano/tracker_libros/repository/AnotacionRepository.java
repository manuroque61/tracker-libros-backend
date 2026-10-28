package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.Anotacion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnotacionRepository
        extends JpaRepository<Anotacion, Long> {

    List<Anotacion> findByUsuario_IdAndLibro_IdOrderByFechaCreacionDesc(
            Long usuarioId,
            Long libroId);

    Optional<Anotacion> findByIdAndUsuario_Id(Long id, Long usuarioId);
}
