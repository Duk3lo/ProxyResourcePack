package org.astral.proxyresourcepack.events.event;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerResourcePackStatusEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.astral.proxyresourcepack.pack.PackManager;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.util.List;
import java.util.UUID;

public class PlayerListener {

    private final ProxyServer proxy;
    private final PackManager packManager;
    private final Logger logger;

    public PlayerListener(ProxyServer proxy, PackManager packManager, Logger logger) {
        this.proxy = proxy;
        this.packManager = packManager;
        this.logger = logger;
    }

    @Subscribe
    public void onServerSwitch(@NonNull ServerPostConnectEvent event) {
        event.getPlayer().getCurrentServer().ifPresent(serverConnection -> {
            String serverName = serverConnection.getServerInfo().getName();
            List<PackManager.PackInfo> packs = packManager.getPacksForServer(serverName);

            if (packs != null && !packs.isEmpty()) {
                for (PackManager.PackInfo packInfo : packs) {
                    UUID packUuid = UUID.nameUUIDFromBytes(packInfo.hash());

                    boolean alreadyApplied = event.getPlayer().getAppliedResourcePacks().stream()
                            .anyMatch(applied -> applied.getId().equals(packUuid));

                    boolean alreadyPending = event.getPlayer().getPendingResourcePacks().stream()
                            .anyMatch(pending -> pending.getId().equals(packUuid));

                    if (alreadyApplied || alreadyPending) {
                        continue;
                    }

                    try {
                        ResourcePackInfo velocityPack = proxy.createResourcePackBuilder(packInfo.downloadUrl())
                                .setHash(packInfo.hash())
                                .setId(packUuid)
                                .setShouldForce(false)
                                .build();

                        event.getPlayer().sendResourcePackOffer(velocityPack);
                        logger.info("Oferta de pack ({}) enviada al jugador {}", packInfo.fileName(), event.getPlayer().getUsername());

                    } catch (Exception e) {
                        logger.error("Error enviando el pack {} a {}", packInfo.fileName(), event.getPlayer().getUsername(), e);
                    }
                }
            }
        });
    }

    @Subscribe
    public void onResourcePackStatus(@NonNull PlayerResourcePackStatusEvent event) {
        switch (event.getStatus()) {
            case SUCCESSFUL ->
                    event.getPlayer().sendMessage(Component.text("✅ Texturas cargadas correctamente.", NamedTextColor.GREEN));
            case FAILED_DOWNLOAD ->
                    event.getPlayer().sendMessage(Component.text("⚠️ Error al descargar texturas.", NamedTextColor.RED));
            case DECLINED ->
                    event.getPlayer().sendMessage(Component.text("❌ Has rechazado el paquete de texturas.", NamedTextColor.YELLOW));
            default -> {}
        }
    }
}