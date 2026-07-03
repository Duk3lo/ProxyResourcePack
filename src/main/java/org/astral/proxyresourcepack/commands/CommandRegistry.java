package org.astral.proxyresourcepack.commands;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.proxy.ProxyServer;
import org.astral.proxyresourcepack.ProxyResourcePack;
import org.astral.proxyresourcepack.commands.subcommands.ImportCmd;
import org.astral.proxyresourcepack.commands.subcommands.InfoCmd;
import org.astral.proxyresourcepack.commands.subcommands.ReloadCmd;
import org.astral.proxyresourcepack.commands.subcommands.StatusCmd;
import org.astral.proxyresourcepack.config.ConfigManager;
import org.astral.proxyresourcepack.pack.PackManager;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.nio.file.Path;

public final class CommandRegistry {

    public static void registerAll(ProxyResourcePack plugin, @NonNull ProxyServer proxy, ConfigManager config, PackManager packManager, Path dataDirectory, Logger logger) {
        MainCommand mainCommand = new MainCommand();

        mainCommand.register(new ReloadCmd(config, packManager));
        mainCommand.register(new StatusCmd(packManager));
        mainCommand.register(new InfoCmd(packManager));
        mainCommand.register(new ImportCmd(dataDirectory, packManager, logger));

        CommandMeta meta = proxy.getCommandManager()
                .metaBuilder("proxyresourcepack")
                .aliases("prp", "packs")
                .plugin(plugin)
                .build();

        proxy.getCommandManager().register(meta, mainCommand);
        logger.info("Comandos registrados correctamente.");
    }
}