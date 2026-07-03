package org.astral.proxyresourcepack.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.astral.proxyresourcepack.pack.PackManager;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ImportCmd implements SubCommand {
    private final Path dataDirectory;
    private final PackManager packManager;
    private final Logger logger;

    public ImportCmd(Path dataDirectory, PackManager packManager, Logger logger) {
        this.dataDirectory = dataDirectory;
        this.packManager = packManager;
        this.logger = logger;
    }

    @Override public String getName() { return "import"; }
    @Override public String getPermission() { return "proxyresourcepack.admin"; }

    @Override
    public void execute(CommandSource source, String @NonNull [] args) {
        if (args.length < 3) {
            source.sendMessage(Component.text("Uso: /prp import <archivo.zip> <nombre_servidor/global>", NamedTextColor.RED));
            return;
        }

        String fileName = args[1];
        String targetServer = args[2].toLowerCase();

        if (!fileName.endsWith(".zip")) {
            source.sendMessage(Component.text("El archivo debe ser .zip", NamedTextColor.RED));
            return;
        }

        Path importFolder = dataDirectory.resolve("import");
        Path src = importFolder.resolve(fileName);

        Path destFolder = targetServer.equals("global") ?
                dataDirectory.resolve("global") :
                dataDirectory.resolve("servers").resolve(targetServer);

        Path dest = destFolder.resolve(fileName);

        if (!Files.exists(src)) {
            source.sendMessage(Component.text("Archivo no encontrado: " + fileName, NamedTextColor.RED));
            return;
        }

        try {
            Files.createDirectories(destFolder);
            Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING);
            packManager.loadPacks();
            source.sendMessage(Component.text("✅ Paquete '" + fileName + "' importado a '" + targetServer + "'.", NamedTextColor.GREEN));
        } catch (Exception e) {
            source.sendMessage(Component.text("Error al mover el archivo. Revisa la consola.", NamedTextColor.DARK_RED));
            logger.error("Error al importar", e);
        }
    }

    @Override
    public List<String> suggest(CommandSource source, String @NonNull [] args) {
        if (args.length == 2) {
            try (Stream<Path> paths = Files.list(dataDirectory.resolve("import"))) {
                return paths.map(p -> p.getFileName().toString())
                        .filter(name -> name.endsWith(".zip") && name.startsWith(args[1]))
                        .collect(Collectors.toList());
            } catch (Exception ignored) {}
        }
        return List.of();
    }
}