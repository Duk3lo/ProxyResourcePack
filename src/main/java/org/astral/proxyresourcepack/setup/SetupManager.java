package org.astral.proxyresourcepack.setup;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public final class SetupManager {

    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final Logger logger;
    private final Gson gson;
    
    private static final String BASE_RESOURCE_DIR = "default-pack/";
    private static final String FONT_TEXTURES_DIR = BASE_RESOURCE_DIR + "assets/minecraft/textures/font/";

    public SetupManager(ProxyServer proxy, Path dataDirectory, Logger logger) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.logger = logger;
        this.gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
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

        logger.info("Exportando ResourcePack interno dinámico...");

        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(targetZip))) {
            List<String> foundImages = findFontImagesInJar();

            if (foundImages.isEmpty()) {
                logger.warn("No se encontraron imágenes en: {}", FONT_TEXTURES_DIR);
            }
            writeResourceToZip(zos, BASE_RESOURCE_DIR + "pack.mcmeta", "pack.mcmeta");
            for (String imagePath : foundImages) {
                String zipPath = imagePath.replace(BASE_RESOURCE_DIR, "");
                writeResourceToZip(zos, imagePath, zipPath);
            }

            writeDynamicFontJson(zos, foundImages);

            logger.info("Pack dinámico exportado con éxito: {} ({} texturas registradas)", targetZip.getFileName(), foundImages.size());

        } catch (Exception e) {
            logger.error("Error al exportar el pack interno", e);
        }
    }

    private @NonNull List<String> findFontImagesInJar() throws Exception {
        List<String> images = new ArrayList<>();
        CodeSource src = SetupManager.class.getProtectionDomain().getCodeSource();

        if (src != null) {
            URL jarUrl = src.getLocation();
            try (ZipInputStream zip = new ZipInputStream(jarUrl.openStream())) {
                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null) break;

                    String name = e.getName();
                    if (name.startsWith(FONT_TEXTURES_DIR) && name.endsWith(".png")) {
                        images.add(name);
                    }
                }
            }
        }
        return images;
    }

    private void writeDynamicFontJson(ZipOutputStream zos, @NonNull List<String> foundImages) throws Exception {
        JsonArray providers = new JsonArray();
        int charCode = 0xE001;

        for (String imagePath : foundImages) {
            String fileName = imagePath.substring(imagePath.lastIndexOf('/') + 1);

            JsonObject provider = new JsonObject();
            provider.addProperty("type", "bitmap");
            provider.addProperty("file", "minecraft:font/" + fileName);
            provider.addProperty("ascent", 38);
            provider.addProperty("height", 44);

            JsonArray chars = new JsonArray();
            chars.add(String.valueOf((char) charCode));
            provider.add("chars", chars);

            providers.add(provider);

            logger.info("Registrada fuente: {} -> \\u{}", fileName, Integer.toHexString(charCode).toUpperCase());
            charCode++;
        }

        JsonObject root = new JsonObject();
        root.add("providers", providers);

        String jsonString = gson.toJson(root);

        ZipEntry entry = new ZipEntry("assets/minecraft/font/default.json");
        zos.putNextEntry(entry);
        zos.write(jsonString.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private void writeResourceToZip(ZipOutputStream zos, String resourcePath, String zipPath) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                logger.warn("No se pudo encontrar el recurso para empaquetar: {}", resourcePath);
                return;
            }
            zos.putNextEntry(new ZipEntry(zipPath));
            is.transferTo(zos);
            zos.closeEntry();
        } catch (Exception e) {
            logger.error("Error escribiendo archivo al ZIP: {}", zipPath, e);
        }
    }
}