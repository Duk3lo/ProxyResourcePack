package org.astral.proxyresourcepack.commands.subcommands;

import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.astral.proxyresourcepack.config.ConfigManager;
import org.astral.proxyresourcepack.pack.PackManager;
import org.jspecify.annotations.NonNull;

import java.util.List;

public final class ReloadCmd implements SubCommand {
    private final ConfigManager configManager;
    private final PackManager packManager;

    public ReloadCmd(ConfigManager configManager, PackManager packManager) {
        this.configManager = configManager;
        this.packManager = packManager;
    }

    @Override public @NonNull String getName() { return "reload"; }
    @Override public @NonNull String getPermission() { return "proxyresourcepack.admin"; }

    @Override
    public void execute(@NonNull CommandSource source, String[] args) {
        source.sendMessage(Component.text("Recargando configuración y paquetes...", NamedTextColor.YELLOW));

        configManager.load();
        packManager.loadPacks();

        source.sendMessage(Component.text("¡Plugin recargado con éxito!", NamedTextColor.GREEN));
    }

    @Override
    public List<String> suggest(CommandSource source, String[] args) {
        return List.of();
    }
}