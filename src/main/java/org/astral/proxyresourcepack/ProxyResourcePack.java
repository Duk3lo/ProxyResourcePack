package org.astral.proxyresourcepack;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.astral.proxyresourcepack.commands.CommandRegistry;
import org.astral.proxyresourcepack.config.ConfigManager;
import org.astral.proxyresourcepack.events.RegisterEvents;
import org.astral.proxyresourcepack.pack.PackManager;
import org.astral.proxyresourcepack.pack.WebServer;
import org.astral.proxyresourcepack.setup.SetupManager;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(id = "proxyresourcepack", name = "ProxyResourcePack", version = "1.0", authors = {"Astral"})
public final class ProxyResourcePack {

    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDirectory;
    private WebServer webServer;

    @Inject
    public ProxyResourcePack(ProxyServer proxy, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ConfigManager configManager = new ConfigManager(dataDirectory, logger);
        configManager.load();

        SetupManager setup = new SetupManager(proxy, dataDirectory, logger);
        setup.init();

        PackManager packManager = new PackManager(logger, dataDirectory, configManager);
        packManager.loadPacks();

        this.webServer = new WebServer(logger, packManager, configManager.getPort());
        this.webServer.start();

        RegisterEvents.registerAll(this, proxy, packManager, logger);
        CommandRegistry.registerAll(this, proxy, configManager, packManager, dataDirectory, logger);

        logger.info("ProxyResourcePack cargado y listo.");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (this.webServer != null) {
            this.webServer.stop();
        }
    }
}