package org.astral.proxyresourcepack.events;

import com.velocitypowered.api.proxy.ProxyServer;
import org.astral.proxyresourcepack.pack.PackManager;
import org.astral.proxyresourcepack.ProxyResourcePack;
import org.astral.proxyresourcepack.events.event.PlayerListener;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

public final class RegisterEvents {

    public static void registerAll(
            @NonNull ProxyResourcePack plugin,
            @NonNull ProxyServer proxy,
            @NonNull PackManager packManager,
            @NonNull Logger logger
    ) {
        var manager = proxy.getEventManager();
        manager.register(plugin, new PlayerListener(proxy, packManager, logger));
    }
}