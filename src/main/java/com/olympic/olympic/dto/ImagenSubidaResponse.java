package com.olympic.olympic.dto;

/** Respuesta de POST /api/imagenes/productos — equivalente a la de api/subir-imagen.php. */
public class ImagenSubidaResponse {

    private String filename;
    private String url;

    public ImagenSubidaResponse() {
    }

    public ImagenSubidaResponse(String filename, String url) {
        this.filename = filename;
        this.url = url;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
