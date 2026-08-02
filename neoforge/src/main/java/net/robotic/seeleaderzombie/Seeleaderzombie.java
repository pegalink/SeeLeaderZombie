package net.robotic.seeleaderzombie;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.robotic.seeleaderzombie.client.ClientConfigScreen;
import net.robotic.seeleaderzombie.config.Config;
import net.robotic.seeleaderzombie.core.LeaderZombies;
import net.robotic.seeleaderzombie.handler.LeaderZombieEvents;
import org.slf4j.Logger;

@Mod(LeaderZombies.MOD_ID)
public final class Seeleaderzombie {

    /** Kept for source compatibility with anything referencing the old constant. */
    public static final String MODID = LeaderZombies.MOD_ID;

    private static final Logger LOGGER = LogUtils.getLogger();

    public Seeleaderzombie(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        NeoForge.EVENT_BUS.register(new LeaderZombieEvents());
        modEventBus.addListener(LeaderZombieEvents::onConfigReloaded);

        // ClientConfigScreen references client-only classes, so it must not be loaded at all on a
        // dedicated server - keeping it in its own class means the JVM only resolves it here.
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ClientConfigScreen.register(modContainer);
        }

        LOGGER.info("SeeLeaderZombie initialised (NeoForge).");
    }
}
