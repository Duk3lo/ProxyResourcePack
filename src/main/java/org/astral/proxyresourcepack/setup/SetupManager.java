package org.astral.proxyresourcepack.setup;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class SetupManager {

    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final Logger logger;

    public SetupManager(ProxyServer proxy, Path dataDirectory, Logger logger) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
    }

    public void init() {
        createDirectories();
        exportDefaultPack();
    }

    private void createDirectories() {
        try {
            Path serversDir = dataDirectory.resolve("servers");
            Path globalDir = dataDirectory.resolve("global");
            Path importDir = dataDirectory.resolve("import");

            Files.createDirectories(serversDir);
            Files.createDirectories(globalDir);
            Files.createDirectories(importDir);

            for (RegisteredServer server : proxy.getAllServers()) {
                Files.createDirectories(serversDir.resolve(server.getServerInfo().getName()));
            }
            logger.info("Estructura de directorios sincronizada.");
        } catch (Exception e) {
            logger.error("Error creando directorios de ResourcePacks", e);
        }
    }

    private void exportDefaultPack() {
        Path targetZip = dataDirectory.resolve("global").resolve("default-logo.zip");

        if (Files.exists(targetZip)) {
            return;
        }

        logger.info("Exportando ResourcePack interno por defecto...");

        String[] filesToZip = {
                "pack.mcmeta",
                "assets/minecraft/font/default.json",
                "assets/minecraft/textures/font/logo.png"
        };

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(targetZip))) {
            for (String file : filesToZip) {
                String resourcePath = "default-pack/" + file;

                try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
                    if (is == null) {
                        logger.warn("No se pudo encontrar el recurso: {}", resourcePath);
                        continue;
                    }

                    zos.putNextEntry(new ZipEntry(file));
                    is.transferTo(zos);
                    zos.closeEntry();
                }
            }
            logger.info("Pack por defecto exportado: {}", targetZip.getFileName());
        } catch (Exception e) {
            logger.error("Error al exportar el pack interno", e);
        }
    }
}