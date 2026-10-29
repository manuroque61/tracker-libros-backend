
package ies.belgrano.tracker_libros.service;

import ies.belgrano.tracker_libros.dto.ArchivoDigitalRespuestaDTO;
import ies.belgrano.tracker_libros.entity.ArchivoDigital;
import ies.belgrano.tracker_libros.entity.Lectura;
import ies.belgrano.tracker_libros.enums.FormatoLectura;
import ies.belgrano.tracker_libros.repository.ArchivoDigitalRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ArchivoDigitalService {

    private static final long MAX_BYTES = 50L * 1024 * 1024;

    private final ArchivoDigitalRepository archivoRepository;
    private final LecturaService lecturaService;
    private final Path directorio;
    private final AlmacenamientoDigitalService almacenamientoDigitalService;

    public ArchivoDigitalService(
            ArchivoDigitalRepository archivoRepository,
            LecturaService lecturaService,
            AlmacenamientoDigitalService almacenamientoDigitalService,
            @Value("${app.storage.location}") String ubicacion) {

        this.archivoRepository = archivoRepository;
        this.lecturaService = lecturaService;
        this.almacenamientoDigitalService = almacenamientoDigitalService;

        this.directorio = Path.of(ubicacion)
                .toAbsolutePath()
                .normalize();
    }

    public ArchivoDigitalRespuestaDTO subirArchivo(
            Long lecturaId,
            Long usuarioId,
            MultipartFile archivo) {

        // La consulta verifica la propiedad de la lectura.
        Lectura lectura = lecturaService.obtenerPorId(
                lecturaId, usuarioId);

        if (lectura.getFormato() == FormatoLectura.FISICO) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Una lectura fisica no admite archivos");
        }

        if (archivo == null || archivo.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El archivo es obligatorio");
        }

        if (archivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "El archivo supera los 50 MB");
        }

        if (archivoRepository.existsByLectura_Id(lecturaId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La lectura ya tiene un archivo");
        }

        String original = archivo.getOriginalFilename();

        if (original == null || original.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El nombre del archivo es obligatorio");
        }

        // No utilizamos rutas proporcionadas por el cliente.
        String nombreSeguro = original
                .replace('\\', '/');

        nombreSeguro = nombreSeguro.substring(
                nombreSeguro.lastIndexOf('/') + 1);

        if (nombreSeguro.isBlank() ||
                nombreSeguro.length() > 255 ||
                nombreSeguro.chars().anyMatch(c -> c < 32)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Nombre de archivo invalido");
        }

        String extension = lectura.getFormato() == FormatoLectura.PDF ? ".pdf" : ".epub";

        if (!nombreSeguro.toLowerCase(Locale.ROOT)
                .endsWith(extension)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La extension no coincide con el formato");
        }

        String tipoContenido = lectura.getFormato() == FormatoLectura.PDF
                ? "application/pdf"
                : "application/epub+zip";

        String nombreAlmacenado = UUID.randomUUID() + extension;

        Path destino = directorio.resolve(nombreAlmacenado);

        try {
            Files.createDirectories(directorio);

            try (var entrada = archivo.getInputStream()) {
                Files.copy(entrada, destino);
            }

            if (Files.size(destino) > MAX_BYTES ||
                    !contenidoValido(destino, lectura.getFormato())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "El contenido no es un PDF o EPUB valido");
            }

            ArchivoDigital registro = new ArchivoDigital();

            registro.setLectura(lectura);
            registro.setNombreOriginal(nombreSeguro);
            registro.setNombreAlmacenado(nombreAlmacenado);
            registro.setTipoContenido(tipoContenido);
            registro.setTamanoBytes(Files.size(destino));

            ArchivoDigital guardado = archivoRepository.saveAndFlush(registro);

            return convertirARespuesta(guardado);

        } catch (IOException e) {
            borrarSilenciosamente(destino);

            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo guardar el archivo", e);

        } catch (RuntimeException e) {
            borrarSilenciosamente(destino);
            throw e;
        }
    }

    @Transactional
    public void eliminarArchivo(
            Long lecturaId,
            Long usuarioId) {

        ArchivoDigital archivo = buscarArchivo(
                lecturaId,
                usuarioId);

        String nombreAlmacenado = archivo.getNombreAlmacenado();

        // Programar limpieza fisica despues del commit.
        almacenamientoDigitalService.eliminarDespuesDelCommit(
                nombreAlmacenado);

        // Eliminar el registro de MySQL.
        archivoRepository.delete(archivo);
        archivoRepository.flush();
    }

    public ArchivoDigitalRespuestaDTO obtenerInformacion(
            Long lecturaId,
            Long usuarioId) {

        ArchivoDigital archivo = buscarArchivo(
                lecturaId,
                usuarioId);

        return convertirARespuesta(archivo);
    }

    public Resource obtenerContenido(
            Long lecturaId,
            Long usuarioId) {

        ArchivoDigital archivo = buscarArchivo(
                lecturaId,
                usuarioId);

        Path ruta = directorio
                .resolve(archivo.getNombreAlmacenado())
                .normalize();

        // Comprobar que la ruta permanezca dentro
        // del directorio privado de almacenamiento.
        if (!ruta.startsWith(directorio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Ruta de archivo invalida");
        }

        try {
            Resource recurso = new UrlResource(ruta.toUri());

            if (!recurso.exists() || !recurso.isReadable()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El archivo no existe en el servidor");
            }

            return recurso;

        } catch (MalformedURLException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "No se pudo acceder al archivo",
                    e);
        }
    }

    private ArchivoDigital buscarArchivo(
            Long lecturaId,
            Long usuarioId) {

        return archivoRepository
                .findByLectura_IdAndLectura_Usuario_Id(
                        lecturaId,
                        usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Archivo digital no encontrado"));
    }

    private boolean contenidoValido(
            Path ruta,
            FormatoLectura formato) throws IOException {

        if (formato == FormatoLectura.PDF) {
            try (var entrada = Files.newInputStream(ruta)) {
                byte[] cabecera = entrada.readNBytes(5);

                return new String(
                        cabecera, StandardCharsets.US_ASCII)
                        .equals("%PDF-");
            }
        }

        if (formato == FormatoLectura.EPUB) {
            try (ZipFile zip = new ZipFile(ruta.toFile())) {
                ZipEntry mimetype = zip.getEntry("mimetype");

                if (mimetype == null || mimetype.isDirectory()) {
                    return false;
                }

                try (var entrada = zip.getInputStream(mimetype)) {
                    byte[] contenido = entrada.readNBytes(21);

                    return new String(
                            contenido, StandardCharsets.US_ASCII)
                            .equals("application/epub+zip");
                }
            } catch (java.util.zip.ZipException e) {
                return false;
            }
        }

        return false;
    }

    private void borrarSilenciosamente(Path ruta) {
        try {
            Files.deleteIfExists(ruta);
        } catch (IOException ignored) {
            // Se deberia registrar el fallo de limpieza.
        }
    }

    private ArchivoDigitalRespuestaDTO convertirARespuesta(
            ArchivoDigital archivo) {

        return new ArchivoDigitalRespuestaDTO(
                archivo.getId(),
                archivo.getLectura().getId(),
                archivo.getNombreOriginal(),
                archivo.getTipoContenido(),
                archivo.getTamanoBytes(),
                archivo.getFechaSubida());
    }
}
