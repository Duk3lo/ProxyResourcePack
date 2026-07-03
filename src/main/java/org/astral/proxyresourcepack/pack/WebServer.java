package org.astral.proxyresourcepack.pack;

import com.sun.net.httpserver.HttpServer;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public final class WebServer {
    private HttpServer server;
    private final Logger logger;
    private final PackManager packManager;
    private final int port;

    public WebServer(Logger logger, PackManager packManager, int port) {
        this.logger = logger;
        this.packManager = packManager;
        this.port = port;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/packs/", exchange -> {
                String path = exchange.getRequestURI().getPath();
                String[] parts = path.split("/");

                if (parts.length < 4) {
                    sendError(exchange, "Invalid path format.");
                    return;
                }

                String serverName = parts[2];
                String fileName = parts[3];

                PackManager.PackInfo packInfo = packManager.getSpecificPack(serverName, fileName);

                if (packInfo == null || packInfo.file() == null || !Files.exists(packInfo.file())) {
                    sendError(exchange, "Pack file not found.");
                    return;
                }

                exchange.getResponseHeaders().set("Content-Type", "application/zip");
                exchange.sendResponseHeaders(200, Files.size(packInfo.file()));

                try (OutputStream os = exchange.getResponseBody()) {
                    Files.copy(packInfo.file(), os);
                }
            });

            server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
            server.start();
            logger.info("Servidor HTTP iniciado en el puerto {}", port);

        } catch (IOException e) {
            logger.error("No se pudo iniciar el servidor web: {}", e.getMessage());
        }
    }

    private void sendError(com.sun.net.httpserver.@NonNull HttpExchange exchange, @NonNull String msg) throws IOException {
        exchange.sendResponseHeaders(404, msg.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(msg.getBytes());
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }
}