
package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.Lectura;
import ies.belgrano.tracker_libros.enums.EstadoLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    List<Lectura> findByUsuario_IdInAndEstado(List<Long> usuarioIds, EstadoLectura estado);

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

    // Comprobar si el libro tiene lecturas asociadas
    boolean existsByLibro_Id(Long libroId);

    // Contar libros distintos del usuario (con filtro opcional de formato)
    @Query("""
            SELECT COUNT(DISTINCT l.libro.id)
            FROM Lectura l
            WHERE l.usuario.id = :usuarioId
              AND (:formato IS NULL OR l.formato = :formato)
            """)
    long contarLibrosDistintos(
            @Param("usuarioId") Long usuarioId,
            @Param("formato") FormatoLectura formato);

    // Contar lecturas del usuario agrupadas por estado
    @Query("""
            SELECT l.estado, COUNT(l)
            FROM Lectura l
            WHERE l.usuario.id = :usuarioId
              AND (:formato IS NULL OR l.formato = :formato)
            GROUP BY l.estado
            """)
    List<Object[]> contarPorEstado(
            @Param("usuarioId") Long usuarioId,
            @Param("formato") FormatoLectura formato);

    // Contar lecturas del usuario agrupadas por formato (sin filtro)
    @Query("""
            SELECT l.formato, COUNT(l)
            FROM Lectura l
            WHERE l.usuario.id = :usuarioId
            GROUP BY l.formato
            """)
    List<Object[]> contarPorFormato(
            @Param("usuarioId") Long usuarioId);
}
