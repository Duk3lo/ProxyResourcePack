package org.astral.proxyresourcepack.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.astral.proxyresourcepack.pack.PackManager;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class InfoCmd implements SubCommand {
    private final PackManager packManager;

    public InfoCmd(PackManager packManager) {
        this.packManager = packManager;
    }

    @Override
    public @NonNull String getName() { return "info"; }

    @Override
    public @NonNull String getPermission() { return "proxyresourcepack.admin"; }

    @Override
    public void execute(@NonNull CommandSource source, String @NonNull [] args) {
        if (args.length < 2) {
            source.sendMessage(Component.text("Uso: /prp info <global|nombre_servidor>", NamedTextColor.RED));
            return;
        }

        String targetId = args[1].toLowerCase();

        List<PackManager.PackInfo> packs = packManager.getExactPacks(targetId);

        if (packs == null || packs.isEmpty()) {
            source.sendMessage(Component.text("No se encontró ningún paquete para: " + targetId, NamedTextColor.RED));
            return;
        }

        source.sendMessage(Component.text("=== Información de: " + targetId + " ===", NamedTextColor.GOLD));

        for (PackManager.PackInfo pack : packs) {
            String packType = (pack.file() == null) ? "URL Externa" : "Local";
            String hashString = bytesToHex(pack.hash());

            source.sendMessage(Component.text("📦 Archivo: ", NamedTextColor.YELLOW).append(Component.text(pack.fileName(), NamedTextColor.GREEN)));
            source.sendMessage(Component.text("   ├─ Tipo: ", NamedTextColor.GRAY).append(Component.text(packType, NamedTextColor.WHITE)));
            source.sendMessage(Component.text("   ├─ Hash: ", NamedTextColor.GRAY).append(Component.text(hashString, NamedTextColor.WHITE)));
            source.sendMessage(Component.text("   └─ URL: ", NamedTextColor.GRAY).append(Component.text(pack.downloadUrl(), NamedTextColor.AQUA)));
        }
    }

    @Override
    public List<String> suggest(CommandSource source, String @NonNull [] args) {
        if (args.length == 2) {
            List<String> suggestions = new ArrayList<>();
            suggestions.add("global");
            suggestions.addAll(packManager.getLoadedServers());
            return suggestions.stream()
                    .filter(name -> name.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    private @NonNull String bytesToHex(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return "Ninguno/Error";
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}