
package ies.belgrano.tracker_libros.repository;

import ies.belgrano.tracker_libros.entity.ArchivoDigital;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArchivoDigitalRepository
        extends JpaRepository<ArchivoDigital, Long> {

    Optional<ArchivoDigital> findByLectura_IdAndLectura_Usuario_Id(
            Long lecturaId,
            Long usuarioId
    );

    boolean existsByLectura_Id(Long lecturaId);
}
