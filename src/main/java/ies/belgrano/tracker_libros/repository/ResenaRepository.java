package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.Resena;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByUsuario_Id(Long usuarioId);

    Optional<Resena> findByUsuario_IdAndLibro_Id(Long usuarioId, Long libroId);
}
