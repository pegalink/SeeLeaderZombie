package net.robotic.seeleaderzombie.handler;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.robotic.seeleaderzombie.config.Config;
import net.robotic.seeleaderzombie.core.LeaderZombies;
import org.slf4j.Logger;

/** Bridges NeoForge's game events onto the shared {@link LeaderZombies} logic. */
public final class LeaderZombieEvents {

    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Runs once the server is fully started. {@code ServerStartingEvent} fires before the levels
     * exist, so scanning there would never find anything.
     */
    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        LeaderZombies.resyncLoadedEntities(event.getServer(), Config.INSTANCE, LOGGER);
        LOGGER.info("SeeLeaderZombie is active on this server.");
    }

    @SubscribeEvent
    public void onEntityJoinLevel(EntityJoinLevelEvent event) {
        // Everything below touches server-authoritative state (attributes, effects, removal).
        if (event.getLevel().isClientSide()) {
            return;
        }
        LeaderZombies.onEntityJoinedLevel(event.getEntity(), Config.INSTANCE, LOGGER);
    }

    /**
     * Applies a config change to mobs that are already loaded, so editing the config in-game
     * takes effect immediately instead of only on newly spawned leaders.
     */
    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        // Listener order relative to Config's own reload handler is not guaranteed, so make sure
        // the cached values are current before acting on them.
        Config.refresh();

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        LeaderZombies.resyncLoadedEntities(server, Config.INSTANCE, LOGGER);
    }
}
