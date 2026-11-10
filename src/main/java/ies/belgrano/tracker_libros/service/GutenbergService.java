package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.GutenbergLibroDTO;
import ies.belgrano.tracker_libros.dto.GutenbergPaginaDTO;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GutenbergService {

    private static final Logger log =
            LoggerFactory.getLogger(GutenbergService.class);

    private static final int MAX_CACHE = 100;
    private static final Duration TTL_CACHE = Duration.ofMinutes(10);
    private static final int TAMANO_PAGINA = 32;
    private static final String RAPIDAPI_HOST = "project-gutenberg-free-books-api1.p.rapidapi.com";
    private record EntradaCache(Instant expira, GutenbergPaginaDTO pagina) {}

    private final RestClient rapidApiClient;
    private final String rapidApiKey;
    private final Map<String, EntradaCache> cache;

    public GutenbergService(@Value("${gutenberg.rapidapi.key:}") String rapidApiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);
        this.rapidApiClient = RestClient.builder()
                .baseUrl("https://" + RAPIDAPI_HOST)
                .requestFactory(factory)
                .build();
        this.rapidApiKey = rapidApiKey == null ? "" : rapidApiKey.trim();

        this.cache = Collections.synchronizedMap(
                new LinkedHashMap<>(16, 0.75f, false) {
                        @Override
                        protected boolean removeEldestEntry(
                                        Map.Entry<String, EntradaCache> eldest) {
                                return size() > MAX_CACHE;
                        }
                });
    }

    public List<GutenbergLibroDTO> buscarLibros(String idioma, String busqueda) {
        return buscarPagina(idioma, busqueda, 1).libros();
    }

    public List<GutenbergLibroDTO> buscarLibros(String idioma, String busqueda, int pagina) {
        return buscarPagina(idioma, busqueda, pagina).libros();
    }

    public GutenbergPaginaDTO buscarPagina(String idioma, String busqueda, int pagina) {

        String clave = (idioma == null ? "" : idioma) + "|"
                + (busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT)) + "|" + pagina;

        Instant ahora = Instant.now();
        EntradaCache guardada = cache.get(clave);

        if (guardada != null && guardada.expira().isAfter(ahora)) {
            return guardada.pagina();
        }

        GutenbergPaginaDTO resultados = consultarRapidApi(idioma, busqueda, pagina);
        cache.put(clave, new EntradaCache(ahora.plus(TTL_CACHE), resultados));
        return resultados;
    }

    private GutenbergPaginaDTO consultarRapidApi(String idioma, String busqueda, int pagina) {
        if (rapidApiKey.isBlank()) {
            throw new IllegalStateException("Falta configurar la variable GUTENBERG_RAPIDAPI_KEY en el entorno del backend.");
        }
        try {
            Map<?, ?> respuesta = rapidApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/books")
                            .queryParam("include_removed", "true")
                            .queryParam("page_size", TAMANO_PAGINA)
                            .queryParam("page", Math.max(1, pagina))
                            .queryParamIfPresent("q", java.util.Optional.ofNullable(busqueda)
                                    .map(String::trim).filter(s -> !s.isBlank()))
                            .build())
                    .header("x-rapidapi-host", RAPIDAPI_HOST)
                    .header("x-rapidapi-key", rapidApiKey)
                    .retrieve()
                    .body(Map.class);

            if (respuesta == null) {
                throw new IllegalStateException("Gutendex devolvió una respuesta vacía");
            }

            Object resultados = respuesta.get("results");
            if (!(resultados instanceof List<?> lista)) {
                throw new IllegalStateException("Gutendex devolvió un formato inesperado");
            }

            List<GutenbergLibroDTO> libros = new ArrayList<>();
            for (Object item : lista) {
                if (!(item instanceof Map<?, ?> doc)) continue;
                GutenbergLibroDTO libro = mapearRapidApi(doc, idioma);
                if (libro != null) libros.add(libro);
            }

            boolean haySiguiente = respuesta.get("next") instanceof String next && !next.isBlank();
            if (!haySiguiente) haySiguiente = libros.size() >= TAMANO_PAGINA;
            return new GutenbergPaginaDTO(libros, haySiguiente);
        } catch (Exception e) {
            EntradaCache anterior = cache.get((idioma == null ? "" : idioma) + "|"
                    + (busqueda == null ? "" : busqueda.trim().toLowerCase(Locale.ROOT)) + "|" + pagina);
            if (anterior != null && !anterior.pagina().libros().isEmpty()) {
                log.warn("Gutendex no responde ({}); se usa una página guardada en caché", e.getMessage());
                return anterior.pagina();
            }
            log.warn("RapidAPI Gutenberg no responde ({}); no se muestran libros de ejemplo", e.getMessage());
            throw new IllegalStateException("No se pudo consultar el catálogo Gutenberg en RapidAPI. Revisa la clave, los límites del plan y vuelve a intentar.", e);
        }
    }

    private GutenbergLibroDTO mapearRapidApi(Map<?, ?> doc, String idiomaSolicitado) {
        Number id = obtenerNumero(doc, "id");
        if (id == null) return null;
        String titulo = obtenerTexto(doc, "title");
        if (titulo == null || titulo.isBlank()) return null;

        List<String> autores = new ArrayList<>();
        Object autoresRaw = doc.get("authors");
        if (autoresRaw instanceof List<?> listaAutores) {
            for (Object autor : listaAutores) {
                String nombre = autor instanceof Map<?, ?> mapa
                        ? obtenerTexto(mapa, "name")
                        : autor instanceof String texto ? texto : null;
                if (nombre != null && !nombre.isBlank()) autores.add(nombre);
            }
        }

        Map<?, ?> formats = doc.get("formats") instanceof Map<?, ?> mapa ? mapa : Map.of();
        String idLibro = id.toString();
        String epub = obtenerTexto(formats, "application/epub+zip");
        String pdf = obtenerTexto(formats, "application/pdf");
        String portada = obtenerTexto(doc, "cover_image");
        if (portada == null) portada = obtenerTexto(formats, "image/jpeg");
        if (epub == null) epub = "https://www.gutenberg.org/ebooks/" + idLibro + ".epub.images";

        Object isbnRaw = doc.get("isbn");
        String isbn = isbnRaw instanceof String texto ? texto
                : isbnRaw instanceof List<?> lista && !lista.isEmpty() && lista.get(0) instanceof String texto ? texto
                : null;
        String fecha = obtenerTexto(doc, "issued");
        if (fecha == null) fecha = obtenerTexto(doc, "publication_date");

        return new GutenbergLibroDTO(
                idLibro,
                titulo,
                autores,
                extraerPrimerIdioma(doc.get("languages"), doc.get("language"), idiomaSolicitado),
                obtenerListaTexto(doc.get("subjects")),
                isbn,
                fecha,
                obtenerTexto(doc, "summary"),
                null,
                portada,
                epub,
                pdf,
                portada
        );
    }

    private String obtenerTexto(Map<?, ?> datos, String clave) {
        Object valor = datos.get(clave);
        return valor instanceof String texto ? texto : null;
    }

    private String extraerPrimerIdioma(Object valor, Object idioma, String predeterminado) {
        if (idioma instanceof String texto && !texto.isBlank()) return texto;
        if (valor instanceof List<?> lista && !lista.isEmpty()) {
            Object primero = lista.get(0);
            if (primero instanceof String s) return s;
        }
        return predeterminado == null ? "es" : predeterminado;
    }

    private List<String> obtenerListaTexto(Object valor) {
        if (!(valor instanceof List<?> lista)) return List.of();
        return lista.stream().filter(String.class::isInstance).map(String.class::cast).toList();
    }

    private Number obtenerNumero(Map<?, ?> datos, String clave) {
        Object valor = datos.get(clave);
        return valor instanceof Number n ? n : null;
    }
}
