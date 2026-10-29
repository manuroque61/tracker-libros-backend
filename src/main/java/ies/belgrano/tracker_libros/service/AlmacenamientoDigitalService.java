
package ies.belgrano.tracker_libros.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class AlmacenamientoDigitalService {

    private static final Logger log =
            LoggerFactory.getLogger(AlmacenamientoDigitalService.class);

    private final Path directorio;

    public AlmacenamientoDigitalService(
            @Value("${app.storage.location}") String ubicacion) {

        this.directorio = Path.of(ubicacion)
                .toAbsolutePath()
                .normalize();
    }

    public void eliminarDespuesDelCommit(String nombreAlmacenado) {

        Path ruta = directorio
                .resolve(nombreAlmacenado)
                .normalize();

        if (!ruta.startsWith(directorio)
                || !ruta.getParent().equals(directorio)) {
            throw new IllegalArgumentException(
                    "Ruta de almacenamiento invalida");
        }

        if (!TransactionSynchronizationManager
                .isSynchronizationActive()
                || !TransactionSynchronizationManager
                .isActualTransactionActive()) {

            throw new IllegalStateException(
                    "Se necesita una transaccion activa");
        }

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        try {
                            Files.deleteIfExists(ruta);
                        } catch (IOException e) {
                            log.error(
                                    "No se pudo eliminar el archivo {}",
                                    ruta,
                                    e
                            );
                        }
                    }
                }
        );
    }
}
