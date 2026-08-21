package com.olympic.olympic.service.impl;

import com.olympic.olympic.dto.ImagenSubidaResponse;
import com.olympic.olympic.exception.ImagenInvalidaException;
import com.olympic.olympic.service.ImagenProductoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Equivalente Java de api/subir-imagen.php: valida extensión y tamaño,
 * genera un nombre único (prod_<uuid>.<ext>) y lo guarda en el directorio
 * configurable app.upload.dir. La URL devuelta es relativa (/uploads/<archivo>)
 * para no depender de una IP fija, a diferencia del PHP original.
 */
@Service
public class ImagenProductoServiceImpl implements ImagenProductoService {

    private static final String PREFIJO_URL = "/uploads/";

    private final Path directorioSubida;
    private final long tamanoMaximoBytes;
    private final List<String> extensionesPermitidas;

    public ImagenProductoServiceImpl(
            @Value("${app.upload.dir}") String uploadDir,
            @Value("${app.upload.max-size-bytes}") long tamanoMaximoBytes,
            @Value("${app.upload.allowed-extensions}") String extensionesPermitidas
    ) {
        this.directorioSubida = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.tamanoMaximoBytes = tamanoMaximoBytes;
        this.extensionesPermitidas = List.of(extensionesPermitidas.toLowerCase().split(","));

        try {
            Files.createDirectories(this.directorioSubida);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear el directorio de subida: " + this.directorioSubida, e);
        }
    }

    @Override
    public ImagenSubidaResponse subir(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new ImagenInvalidaException("No se recibió imagen");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = extraerExtension(nombreOriginal);

        if (!extensionesPermitidas.contains(extension)) {
            throw new ImagenInvalidaException("Formato no permitido. Usa JPG, PNG o WEBP");
        }

        if (archivo.getSize() > tamanoMaximoBytes) {
            throw new ImagenInvalidaException("La imagen no debe superar 5MB");
        }

        String nombreFinal = "prod_" + UUID.randomUUID() + "." + extension;
        Path destino = directorioSubida.resolve(nombreFinal);

        try {
            archivo.transferTo(destino);
        } catch (IOException e) {
            throw new ImagenInvalidaException("Error al guardar la imagen");
        }

        return new ImagenSubidaResponse(nombreFinal, PREFIJO_URL + nombreFinal);
    }

    private String extraerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            return "";
        }
        return nombreArchivo.substring(nombreArchivo.lastIndexOf('.') + 1).toLowerCase();
    }
}
