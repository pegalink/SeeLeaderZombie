package net.robotic.seeleaderzombie.LeaderZombieHandler;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.robotic.seeleaderzombie.Config;
import net.robotic.seeleaderzombie.utils.SetEntityName;

import org.slf4j.Logger;

public final class LeaderZombieConfigSync {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean isSupportedLeaderMob(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.ZOMBIE || type == EntityType.DROWNED || type == EntityType.ZOMBIFIED_PIGLIN;
    }

    public static void onConfigReloaded(ModConfigEvent.Reloading event) {
        MinecraftServer server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        if (!Config.enableLeaderZombies) {
            int removed = 0;

            for (var level : server.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof LivingEntity livingEntity
                            && isSupportedLeaderMob(livingEntity)
                            && LeaderZombieDetect.isLeaderZombieStatic(livingEntity)) {
                        entity.discard();
                        removed++;
                    }
                }
            }

            if (Config.logging) {
                LOGGER.info("Removed {} leader zombie(s) due to config enableLeaderZombies=false", removed);
            }
        }
        if (Config.useName) {
            for (var level : server.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof LivingEntity livingEntity
                        && isSupportedLeaderMob(livingEntity)
                        && LeaderZombieDetect.isLeaderZombieStatic(livingEntity)) {
                        if (livingEntity.hasEffect(MobEffects.GLOWING)) {
                            livingEntity.removeEffect(MobEffects.GLOWING);
                        }
                        SetEntityName.setEntityName(livingEntity, "Leader Zombie");
                    }
                }
            }
        }
        if (!Config.useName) {
            for (var level : server.getAllLevels()) {
                for (Entity entity : level.getAllEntities()) {
                    if (entity instanceof LivingEntity livingEntity
                        && isSupportedLeaderMob(livingEntity)
                        && LeaderZombieDetect.isLeaderZombieStatic(livingEntity)) {
                        SetEntityName.setEntityName(livingEntity, null);
                        int ticks = Integer.MAX_VALUE;
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false));
                    }
                }
            }
        }
    }
}