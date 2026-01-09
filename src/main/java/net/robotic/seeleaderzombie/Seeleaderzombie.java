package net.robotic.seeleaderzombie;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.robotic.seeleaderzombie.LeaderZombieHandler.LeaderZombieConfigSync;
import net.robotic.seeleaderzombie.LeaderZombieHandler.LeaderZombieDetect;
import org.slf4j.Logger;

@Mod(Seeleaderzombie.MODID)
public class Seeleaderzombie {
    public static final String MODID = "seeleaderzombie";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Seeleaderzombie(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(new LeaderZombieDetect());
        modEventBus.addListener(LeaderZombieConfigSync::onConfigReloaded);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("The mod See Leader Zombies is loaded!");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.warn("This mod is not necessary unless you playing single player!");
        }
    }
}
