package com.olympic.olympic.config;

import java.io.IOException;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class BrowserLauncher {

    @EventListener(ApplicationReadyEvent.class)
    public void abrirNavegador() {

        String url = "http://localhost:8080/";

        System.out.println("========================================");
        System.out.println("Aplicación iniciada correctamente");
        System.out.println("Abriendo navegador: " + url);
        System.out.println("========================================");

        try {
            new ProcessBuilder(
                    "cmd",
                    "/c",
                    "start",
                    "",
                    url
            ).start();

        } catch (IOException e) {
            System.out.println("No se pudo abrir el navegador automáticamente.");
            System.out.println("Abre manualmente: " + url);
            e.printStackTrace();
        }
    }
}
