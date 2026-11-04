package ies.belgrano.tracker_libros.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {

    private static final int MAX_CLAVES = 10_000;

    private record Ventana(Instant expira, int intentos) {}

    private final ConcurrentHashMap<String, Ventana> ventanas =
            new ConcurrentHashMap<>();

    // Rechaza la peticion (429) si la clave supero el maximo de intentos
    public void verificar(String clave, int maxIntentos) {

        Ventana ventana = ventanas.get(clave);

        if (ventana == null) {
            return;
        }

        if (ventana.expira().isBefore(Instant.now())) {
            ventanas.remove(clave, ventana);
            return;
        }

        if (ventana.intentos() >= maxIntentos) {
            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Demasiados intentos. Intentalo de nuevo mas tarde");
        }
    }

    // Registra un intento dentro de la ventana de la clave
    public void contar(String clave, Duration duracion) {

        ventanas.compute(clave, (k, v) -> {
            Instant ahora = Instant.now();

            if (v == null || v.expira().isBefore(ahora)) {
                return new Ventana(ahora.plus(duracion), 1);
            }

            return new Ventana(v.expira(), v.intentos() + 1);
        });

        if (ventanas.size() > MAX_CLAVES) {
            Instant ahora = Instant.now();
            ventanas.entrySet()
                    .removeIf(e -> e.getValue().expira().isBefore(ahora));
        }
    }

    // Reinicia la ventana de la clave (ej. tras un login correcto)
    public void limpiar(String clave) {
        ventanas.remove(clave);
    }
}
