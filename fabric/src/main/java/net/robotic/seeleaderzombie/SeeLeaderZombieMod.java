package net.robotic.seeleaderzombie;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.robotic.seeleaderzombie.command.SeeLeaderZombieCommand;
import net.robotic.seeleaderzombie.config.ModConfig;
import net.robotic.seeleaderzombie.core.LeaderZombies;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SeeLeaderZombieMod implements ModInitializer {

    public static final String MOD_ID = LeaderZombies.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModConfig.load();

        // Both events below are fired on the logical server only, in singleplayer and on a
        // dedicated server alike, which is exactly where entity state may be touched.
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            LeaderZombies.resyncLoadedEntities(server, ModConfig.INSTANCE, LOGGER);
            LOGGER.info("SeeLeaderZombie is active on this server.");
        });

        ServerEntityEvents.ENTITY_LOAD.register((entity, level) ->
                LeaderZombies.onEntityJoinedLevel(entity, ModConfig.INSTANCE, LOGGER));

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                SeeLeaderZombieCommand.register(dispatcher));

        LOGGER.info("SeeLeaderZombie initialised (Fabric).");
    }
}
