package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.GutenbergLibroDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class GutenbergService {

    private final RestClient restClient;

    public GutenbergService() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(8000);
        this.restClient = RestClient.builder()
                .baseUrl("https://gutendex.com")
                .requestFactory(factory)
                .build();
    }

    public List<GutenbergLibroDTO> buscarLibros(String idioma, String busqueda) {
        try {
            Map<?, ?> respuesta = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/books")
                            .queryParam("language", idioma)
                            .queryParam("limit", 200)
                            .build())
                    .retrieve()
                    .body(Map.class);

            if (respuesta == null) {
                return obtenerLibrosEjemplo(busqueda);
            }

            Object resultados = respuesta.get("results");
            if (!(resultados instanceof List<?> lista)) {
                return obtenerLibrosEjemplo(busqueda);
            }

            List<GutenbergLibroDTO> libros = new ArrayList<>();
            int anioActual = java.time.Year.now().getValue();

            for (Object item : lista) {
                if (!(item instanceof Map<?, ?> doc)) continue;

                String titulo = obtenerTexto(doc, "title");
                String fechaPublicacion = obtenerTexto(doc, "publication_date");

                boolean dominioPublico = false;
                Object autoresObj = doc.get("authors");
                if (autoresObj instanceof List<?> autores) {
                    int maxMuerte = 0;
                    boolean tieneMuerte = false;
                    for (Object a : autores) {
                        if (a instanceof Map<?, ?> autorMap) {
                            Object muerte = autorMap.get("death_year");
                            if (muerte instanceof String m && !m.isBlank()) {
                                try {
                                    int año = Integer.parseInt(m);
                                    if (año > maxMuerte) maxMuerte = año;
                                    tieneMuerte = true;
                                } catch (NumberFormatException e) {
                                }
                            }
                        }
                    }
                    if (tieneMuerte && (maxMuerte + 70 <= anioActual)) {
                        dominioPublico = true;
                    } else if (!tieneMuerte && fechaPublicacion != null) {
                        try {
                            int pubYear = Integer.parseInt(fechaPublicacion.substring(0, 4));
                            if (pubYear + 70 <= anioActual) dominioPublico = true;
                        } catch (Exception e) {
                        }
                    }
                }

                if (!dominioPublico) continue;

                if (busqueda != null && !busqueda.isBlank()) {
                    String texto = (titulo != null ? titulo : "") + " ";
                    if (autoresObj instanceof List<?> autores) {
                        for (Object a : autores) {
                            if (a instanceof Map<?, ?> autorMap) {
                                texto += obtenerTexto(autorMap, "name") + " ";
                            }
                        }
                    }
                    if (!texto.toLowerCase().contains(busqueda.toLowerCase())) continue;
                }

                String portada = null;
                Object formats = doc.get("formats");
                if (formats instanceof Map<?, ?> fmtMap) {
                    Object epub = fmtMap.get("application/epub+zip");
                    Object pdf = fmtMap.get("application/pdf");
                    portada = (epub instanceof String) ? (String) epub :
                            (pdf instanceof String ? (String) pdf : null);
                }

                String urlEpub = null;
                String urlPdf = null;
                if (formats instanceof Map<?, ?> fmtMap2) {
                    Object epub = fmtMap2.get("application/epub+zip");
                    Object pdf = fmtMap2.get("application/pdf");
                    urlEpub = (epub instanceof String) ? (String) epub : null;
                    urlPdf = (pdf instanceof String) ? (String) pdf : null;
                }

                List<String> nombresAutores = new ArrayList<>();
                if (autoresObj instanceof List<?> autores) {
                    for (Object a : autores) {
                        if (a instanceof Map<?, ?> autorMap) {
                            String nombre = obtenerTexto(autorMap, "name");
                            if (nombre != null) nombresAutores.add(nombre);
                        }
                    }
                }

                String isbn = null;
                Object isbns = doc.get("isbn");
                if (isbns instanceof List<?> isbnLista && !isbnLista.isEmpty()) {
                    Object primero = isbnLista.get(0);
                    if (primero instanceof String s) isbn = s;
                }

                String idiomaLibro = obtenerTexto(doc, "languages") != null ?
                        extraerPrimerIdioma(doc.get("languages")) : idioma;

                libros.add(new GutenbergLibroDTO(
                        String.valueOf(obtenerNumero(doc, "id")),
                        titulo,
                        nombresAutores,
                        idiomaLibro,
                        null,
                        isbn,
                        fechaPublicacion,
                        null,
                        null,
                        null,
                        urlEpub,
                        urlPdf,
                        portada
                ));
            }

            if (!libros.isEmpty()) {
                return libros;
            }
        } catch (Exception e) {
            // Si gutendex falla, usar ejemplos
        }
        return obtenerLibrosEjemplo(busqueda);
    }

    private List<GutenbergLibroDTO> obtenerLibrosEjemplo(String busqueda) {
        List<GutenbergLibroDTO> ejemplos = new ArrayList<>();
        ejemplos.add(new GutenbergLibroDTO(
                "1342", "Orgullo y prejuicio",
                List.of("Jane Austen"), "es", null, null, "1813",
                "Novela de Jane Austen publicada en 1813.", null, null,
                null, null, null));
        ejemplos.add(new GutenbergLibroDTO(
                "11", "Alicia en el país de las maravillas",
                List.of("Lewis Carroll"), "es", null, null, "1865",
                "Obra clásica de Lewis Carroll.", null, null,
                null, null, null));
        ejemplos.add(new GutenbergLibroDTO(
                "84", "Frankenstein",
                List.of("Mary Shelley"), "es", null, null, "1818",
                "Novela gótica de Mary Shelley.", null, null,
                null, null, null));
        ejemplos.add(new GutenbergLibroDTO(
                "2701", "Moby Dick",
                List.of("Herman Melville"), "es", null, null, "1851",
                "Novela de Herman Melville sobre la caza de ballenas.", null, null,
                null, null, null));

        if (busqueda == null || busqueda.isBlank()) {
            return ejemplos;
        }
        String filtro = busqueda.toLowerCase();
        return ejemplos.stream()
                .filter(l -> l.titulo().toLowerCase().contains(filtro)
                        || l.autores().stream().anyMatch(a -> a.toLowerCase().contains(filtro)))
                .toList();
    }

    private String obtenerTexto(Map<?, ?> datos, String clave) {
        Object valor = datos.get(clave);
        return valor instanceof String texto ? texto : null;
    }

    private String extraerPrimerIdioma(Object valor) {
        if (valor instanceof List<?> lista && !lista.isEmpty()) {
            Object primero = lista.get(0);
            if (primero instanceof String s) return s;
        }
        return "es";
    }

    private Number obtenerNumero(Map<?, ?> datos, String clave) {
        Object valor = datos.get(clave);
        return valor instanceof Number n ? n : null;
    }
}
