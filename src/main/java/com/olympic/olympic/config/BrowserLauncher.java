package com.olympic.olympic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

/**
 * Abre automáticamente el navegador con la app apenas Spring Boot termina
 * de arrancar, para no tener que escribir localhost:8080 a mano.
 */
@Component
public class BrowserLauncher implements ApplicationListener<ApplicationReadyEvent> {

    @Value("${server.port:8080}")
    private String puerto;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        String url = "http://localhost:" + puerto + "/";

        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            System.out.println("Abre manualmente: " + url);
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(url));
        } catch (Exception e) {
            System.out.println("Abre manualmente: " + url);
        }
    }
}