package com.olympic.olympic.service;

import com.olympic.olympic.dto.ImagenSubidaResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImagenProductoService {

    /** Valida, guarda en disco y devuelve el nombre de archivo generado + su URL pública. */
    ImagenSubidaResponse subir(MultipartFile archivo);
}
