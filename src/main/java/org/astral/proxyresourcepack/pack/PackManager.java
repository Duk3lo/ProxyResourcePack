package org.astral.proxyresourcepack.pack;

import org.astral.proxyresourcepack.config.ConfigManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class PackManager {
    private final Logger logger;
    private final Path dataDirectory;
    private final ConfigManager config;

    private final Map<String, List<PackInfo>> serverPacks = new HashMap<>();
    private final List<PackInfo> globalPacks = new ArrayList<>();

    public PackManager(Logger logger, Path dataDirectory, ConfigManager config) {
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.config = config;
    }

    public void loadPacks() {
        serverPacks.clear();
        globalPacks.clear();

        String globalExtUrl = config.getExternalUrl("global");
        if (!globalExtUrl.isEmpty()) {
            globalPacks.add(new PackInfo("global", "external", hexToBytes(config.getExternalHash("global")), null, globalExtUrl));
            logger.info("Pack global configurado vía URL externa.");
        } else {
            loadLocalPacksFromFolder("global", dataDirectory.resolve("global"), globalPacks);
        }

        Path serversDir = dataDirectory.resolve("servers");
        try (Stream<Path> paths = Files.list(serversDir)) {
            paths.filter(Files::isDirectory).forEach(serverDir -> {
                String serverName = serverDir.getFileName().toString();
                List<PackInfo> packsForThisServer = new ArrayList<>();

                String extUrl = config.getExternalUrl(serverName);
                if (!extUrl.isEmpty()) {
                    packsForThisServer.add(new PackInfo(serverName, "external", hexToBytes(config.getExternalHash(serverName)), null, extUrl));
                    logger.info("Pack para '{}' configurado vía URL externa.", serverName);
                } else {
                    loadLocalPacksFromFolder(serverName, serverDir, packsForThisServer);
                }

                if (!packsForThisServer.isEmpty()) {
                    serverPacks.put(serverName, packsForThisServer);
                }
            });
        } catch (Exception e) {
            logger.error("Error leyendo carpetas de servidores", e);
        }
    }

    private void loadLocalPacksFromFolder(String categoryName, Path folder, List<PackInfo> targetList) {
        try (Stream<Path> files = Files.list(folder)) {
            files.filter(f -> f.getFileName().toString().endsWith(".zip")).forEach(zip -> {
                String fileName = zip.getFileName().toString();
                String localUrl = "http://" + config.getPublicIp() + ":" + config.getPort() + "/packs/" + categoryName + "/" + fileName;
                targetList.add(new PackInfo(categoryName, fileName, calculateSHA1(zip), zip, localUrl));
                logger.info("Pack cargado [{}]: {}", categoryName, fileName);
            });
        } catch (Exception e) {
            logger.error("Error leyendo archivos zip en {}", categoryName, e);
        }
    }

    public List<PackInfo> getPacksForServer(String serverName) {
        return serverPacks.getOrDefault(serverName, globalPacks);
    }

    public @Nullable PackInfo getSpecificPack(@NonNull String serverName, String fileName) {
        List<PackInfo> packs = serverName.equals("global") ? globalPacks : serverPacks.get(serverName);
        if (packs == null) return null;
        for (PackInfo pack : packs) {
            if (pack.fileName().equals(fileName)) return pack;
        }
        return null;
    }

    private byte[] calculateSHA1(Path file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream fis = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int n;
                while ((n = fis.read(buffer)) != -1) digest.update(buffer, 0, n);
            }
            return digest.digest();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private byte @NonNull [] hexToBytes(String s) {
        if (s == null || s.isEmpty()) return new byte[0];
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public java.util.Set<String> getLoadedServers() {
        return serverPacks.keySet();
    }

    public List<PackInfo> getExactPacks(String id) {
        if ("global".equals(id)) return globalPacks;
        return serverPacks.get(id);
    }

    public record PackInfo(String serverId, String fileName, byte[] hash, Path file, String downloadUrl) {}
}