package net.robotic.seeleaderzombie.LeaderZombieHandler;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.robotic.seeleaderzombie.Config;
import org.slf4j.Logger;

public final class LeaderZombieConfigSync {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        if (!Config.enableLeaderZombies) {
            int removed = 0;

            for (var level : server.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if ((entity instanceof Zombie || entity instanceof Drowned || entity instanceof ZombifiedPiglin)
                            && LeaderZombieDetect.isLeaderZombieStatic((LivingEntity) entity)) {
                        entity.discard();
                        removed++;
                    }
                }
            }

            if (Config.logging) {
                LOGGER.info("Removed {} leader zombie(s) due to config enableLeaderZombies=false", removed);
            }
        }
    }
}
