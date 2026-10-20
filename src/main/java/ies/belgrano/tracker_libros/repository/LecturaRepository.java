
package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.Lectura;
import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LecturaRepository
        extends JpaRepository<Lectura, Long> {

    // Obtener todas las lecturas de un usuario
    List<Lectura> findByUsuario_Id(Long usuarioId);

    // Buscar una lectura por ID y usuario
    Optional<Lectura> findByIdAndUsuario_Id(
            Long id,
            Long usuarioId
    );

    // Filtrar las lecturas de un usuario por estado
    List<Lectura> findByUsuario_IdAndEstado(
            Long usuarioId,
            EstadoLectura estado
    );

    // Filtrar las lecturas de un usuario por formato
    List<Lectura> findByUsuario_IdAndFormato(
            Long usuarioId,
            FormatoLectura formato
    );

    // Comprobar si existe una lectura de ese libro y formato
    boolean existsByUsuario_IdAndLibro_IdAndFormato(
            Long usuarioId,
            Long libroId,
            FormatoLectura formato
    );
}
