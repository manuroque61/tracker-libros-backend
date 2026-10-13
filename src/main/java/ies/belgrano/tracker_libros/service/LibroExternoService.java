
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.LibroExternoDTO;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LibroExternoService {

    private final RestClient restClient;

    public LibroExternoService() {
        this.restClient = RestClient.builder()
                .baseUrl("https://openlibrary.org")
                .build();
    }

    public LibroExternoDTO buscarPorIsbn(String isbn) {

        Map<?, ?> respuesta = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/books")
                        .queryParam("bibkeys", "ISBN:" + isbn)
                        .queryParam("format", "json")
                        .queryParam("jscmd", "data")
                        .build())
                .retrieve()
                .body(Map.class);

        if (respuesta == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Open Library devolvio una respuesta vacia"
            );
        }

        Object resultado = respuesta.get("ISBN:" + isbn);

        if (!(resultado instanceof Map<?, ?> datos)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No se encontro el libro"
            );
        }

        String titulo = obtenerTexto(datos, "title");
        String autor = obtenerPrimerNombre(datos, "authors");
        String editorial = obtenerPrimerNombre(datos, "publishers");
        String fecha = obtenerTexto(datos, "publish_date");

        Integer paginas = null;
        Object valorPaginas = datos.get("number_of_pages");

        if (valorPaginas instanceof Number numero) {
            paginas = numero.intValue();
        }

        String portada = null;

        if (datos.get("cover") instanceof Map<?, ?> cover) {
            portada = obtenerTexto(cover, "large");
        }

        return new LibroExternoDTO(
                isbn,
                titulo,
                autor,
                null,
                null,
                paginas,
                editorial,
                fecha,
                portada
        );
    }

    private String obtenerTexto(Map<?, ?> datos, String clave) {
        Object valor = datos.get(clave);
        return valor instanceof String texto ? texto : null;
    }

    private String obtenerPrimerNombre(
            Map<?, ?> datos, String clave) {

        Object valor = datos.get(clave);

        if (valor instanceof List<?> lista && !lista.isEmpty()) {
            Object primero = lista.get(0);

            if (primero instanceof Map<?, ?> elemento) {
                return obtenerTexto(elemento, "name");
            }
        }

        return null;
    }
}
