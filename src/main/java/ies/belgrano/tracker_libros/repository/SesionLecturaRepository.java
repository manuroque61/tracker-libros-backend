package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.SesionLectura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SesionLecturaRepository
        extends JpaRepository<SesionLectura, Long> {

    List<SesionLectura> findByLectura_IdOrderByFechaDesc(Long lecturaId);

    List<SesionLectura> findByLectura_Usuario_Id(Long usuarioId);

    void deleteByLectura_Id(Long lecturaId);
}
