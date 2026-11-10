package ies.belgrano.tracker_libros.controller;

import java.net.URI;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/api/gutenberg/epub")
public class GutenbergEpubProxyController {
    private final RestClient gutendex = RestClient.builder().baseUrl("https://gutendex.com").build();
    private final RestClient gutenberg = RestClient.builder().baseUrl("https://www.gutenberg.org").build();

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> epub(@PathVariable long id) {
        if (id <= 0) return ResponseEntity.badRequest().build();

        byte[] bytes = download("https://www.gutenberg.org/ebooks/" + id + ".epub3.images");
        if (bytes == null) bytes = download("https://www.gutenberg.org/ebooks/" + id + ".epub.images");
        if (bytes == null) {
            String url = findEpubUrl(id);
            bytes = url == null ? null : download(url);
        }
        if (bytes == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/epub+zip")).body(bytes);
    }

    private String findEpubUrl(long id) {
        try {
            Map<?, ?> book = gutendex.get().uri("/books/{id}", id).retrieve().body(Map.class);
            if (book == null || !(book.get("formats") instanceof Map<?, ?> formats)) return null;
            Object exact = formats.get("application/epub+zip");
            if (exact instanceof String value && !value.isBlank()) return value;
            return formats.entrySet().stream()
                    .filter(entry -> entry.getKey() != null
                            && entry.getKey().toString().toLowerCase().startsWith("application/epub"))
                    .map(Map.Entry::getValue)
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .findFirst().orElse(null);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private byte[] download(String url) {
        String currentUrl = url;
        for (int redirect = 0; redirect <= 5; redirect++) {
            try {
                DownloadResponse result = gutenberg.get().uri(currentUrl).exchange((request, response) ->
                        new DownloadResponse(
                                response.getStatusCode().value(),
                                response.getHeaders().getFirst(HttpHeaders.LOCATION),
                                response.getStatusCode().is2xxSuccessful()
                                        ? response.getBody().readAllBytes()
                                        : new byte[0]));
                if (result.status() >= 200 && result.status() < 300) {
                    byte[] body = result.body();
                    return body.length > 3 && body[0] == 'P' && body[1] == 'K' ? body : null;
                }
                if (result.status() < 300 || result.status() >= 400 || result.location() == null) return null;
                currentUrl = URI.create(currentUrl).resolve(result.location()).toString();
            } catch (RuntimeException ignored) {
                return null;
            }
        }
        return null;
    }

    private record DownloadResponse(int status, String location, byte[] body) {}
}
