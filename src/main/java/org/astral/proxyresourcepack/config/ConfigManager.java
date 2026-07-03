package org.astral.proxyresourcepack.config;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigManager {
    private final Path configFile;
    private final YamlConfigurationLoader loader;
    private CommentedConfigurationNode root;
    private final Logger logger;

    public ConfigManager(@NonNull Path dataDirectory, Logger logger) {
        this.logger = logger;
        this.configFile = dataDirectory.resolve("config.yml");
        this.loader = YamlConfigurationLoader.builder().path(configFile).build();
    }

    public void load() {
        try {
            if (!Files.exists(configFile)) {
                Files.createDirectories(configFile.getParent());
                root = loader.createNode();

                root.node("http-server", "port").set(8080);
                root.node("http-server", "public-ip").set("127.0.0.1");
                root.node("packs", "ejemplo_servidor", "url").set("https://mi-web.com/paquete.zip");
                root.node("packs", "ejemplo_servidor", "hash").set("AQUI_TU_HASH_EN_HEXADECIMAL");

                loader.save(root);
                logger.info("Archivo config.yml generado por primera vez.");
            } else {
                root = loader.load();
                logger.info("Configuración cargada correctamente.");
            }
        } catch (Exception e) {
            logger.error("Hubo un error crítico al cargar/crear el archivo config.yml", e);
        }
    }

    public int getPort() { return root.node("http-server", "port").getInt(8080); }
    public @NonNull String getPublicIp() { return root.node("http-server", "public-ip").getString("127.0.0.1"); }

    public @NonNull String getExternalUrl(String serverName) { return root.node("packs", serverName, "url").getString(""); }
    public @NonNull String getExternalHash(String serverName) { return root.node("packs", serverName, "hash").getString(""); }
}