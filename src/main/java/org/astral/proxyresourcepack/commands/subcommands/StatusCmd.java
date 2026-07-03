package org.astral.proxyresourcepack.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.astral.proxyresourcepack.pack.PackManager;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class StatusCmd implements SubCommand {
    private final PackManager packManager;

    public StatusCmd(PackManager packManager) {
        this.packManager = packManager;
    }

    @Override public @NonNull String getName() { return "status"; }
    @Override public @NonNull String getPermission() { return "proxyresourcepack.admin"; }

    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        source.sendMessage(Component.text("=== Estado de Resource Packs ===", NamedTextColor.GOLD));

        List<PackManager.PackInfo> globalPacks = packManager.getExactPacks("global");

        if (globalPacks != null && !globalPacks.isEmpty()) {
            source.sendMessage(Component.text("- Global: ", NamedTextColor.YELLOW)
                    .append(Component.text(globalPacks.size() + " paquete(s) cargado(s)", NamedTextColor.GREEN)));
        } else {
            source.sendMessage(Component.text("- Global: NO CONFIGURADO", NamedTextColor.RED));
        }

        source.sendMessage(Component.text("Servidores detectados con pack propio:", NamedTextColor.GOLD));
        for (String serverName : packManager.getLoadedServers()) {
            int amount = packManager.getExactPacks(serverName).size();
            source.sendMessage(Component.text(" > " + serverName + " (" + amount + " packs)", NamedTextColor.GRAY));
        }
    }

    @Override public List<String> suggest(CommandSource source, String[] args) { return List.of(); }
}