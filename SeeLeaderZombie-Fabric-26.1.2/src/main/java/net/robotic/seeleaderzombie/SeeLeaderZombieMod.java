package net.robotic.seeleaderzombie;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.robotic.seeleaderzombie.config.ModConfig;
import net.robotic.seeleaderzombie.handler.LeaderZombieHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeeLeaderZombieMod implements ModInitializer {
    public static final String MOD_ID = "seeleaderzombie";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        // Load configuration
        ModConfig.load();

        // Register server lifecycle events
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LeaderZombieHandler.onServerStart(server);
        });

        // Register entity join world event (fires when entity joins a level)
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            LeaderZombieHandler.onEntityLoad(entity);
        });

        LOGGER.info("SeeLeaderZombie (Fabric) initialized!");
    }
}
