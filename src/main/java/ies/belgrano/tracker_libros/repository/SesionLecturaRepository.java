package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.SesionLectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SesionLecturaRepository
        extends JpaRepository<SesionLectura, Long> {

    List<SesionLectura> findByLectura_IdOrderByFechaDesc(Long lecturaId);

    List<SesionLectura> findByLectura_Usuario_Id(Long usuarioId);

    void deleteByLectura_Id(Long lecturaId);

    // Sumar minutos, paginas y cantidad de sesiones del usuario
    @Query("""
            SELECT COALESCE(SUM(s.minutos), 0),
                   COALESCE(SUM(s.paginasLeidas), 0),
                   COUNT(s)
            FROM SesionLectura s
            WHERE s.lectura.usuario.id = :usuarioId
              AND (:formato IS NULL OR s.lectura.formato = :formato)
            """)
    List<Object[]> sumarTotales(
            @Param("usuarioId") Long usuarioId,
            @Param("formato") FormatoLectura formato);

    // Sumar sesiones del usuario agrupadas por anio y mes
    @Query("""
            SELECT YEAR(s.fecha), MONTH(s.fecha),
                   COALESCE(SUM(s.minutos), 0),
                   COALESCE(SUM(s.paginasLeidas), 0),
                   COUNT(s)
            FROM SesionLectura s
            WHERE s.lectura.usuario.id = :usuarioId
              AND s.fecha >= :desde
              AND (:formato IS NULL OR s.lectura.formato = :formato)
            GROUP BY YEAR(s.fecha), MONTH(s.fecha)
            """)
    List<Object[]> sumarPorMes(
            @Param("usuarioId") Long usuarioId,
            @Param("desde") LocalDateTime desde,
            @Param("formato") FormatoLectura formato);
}
