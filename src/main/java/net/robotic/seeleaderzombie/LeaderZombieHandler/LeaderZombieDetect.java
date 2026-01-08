package net.robotic.seeleaderzombie.LeaderZombieHandler;

import com.mojang.logging.LogUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.robotic.seeleaderzombie.Config;
import org.slf4j.Logger;

import java.util.Objects;

public class LeaderZombieDetect {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double LEADER_REINFORCEMENT_THRESHOLD = 0.1;

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        event.getServer().getAllLevels().forEach(world -> {
            world.getAllEntities().forEach(entity -> {
                if (entity instanceof Zombie zombie && isLeaderZombie(zombie)) {
                    if (!Config.enableLeaderZombies) {
                        zombie.discard();
                        if (Config.logging) {
                            LOGGER.info("Removed a zombie leader due to configuration settings. Leader was at: \\{}", zombie.blockPosition());
                        }
                    } else {
                        giveGlowingEffect(zombie);
                        if (Config.logging) {
                            LOGGER.info("Detected an existing leader zombie at server start at: \\{}", zombie.blockPosition());
                        }
                    }
                }
            });
        });
        if (Config.logging) {
            LOGGER.info("LeaderZombieDetect: checked existing zombies on server start.");
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Zombie zombie && isLeaderZombie(zombie)) {
            if (!Config.enableLeaderZombies) {
                zombie.discard();
                if (Config.logging) {
                    LOGGER.info("Removed a zombie leader due to configuration settings. Leader was spawned at: \\{}", zombie.blockPosition());
                }
            } else {
                giveGlowingEffect(zombie);
                if (Config.logging) {
                    LOGGER.info("Detected a leader zombie spawned at: \\{}", zombie.blockPosition());
                }
            }
        }
    }

    public static boolean isLeaderZombieStatic(Zombie zombie) {
        if (zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE) != null) {
            return Objects.requireNonNull(zombie.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).getValue()
                    > LEADER_REINFORCEMENT_THRESHOLD;
        }
        return false;
    }

    public boolean isLeaderZombie(Zombie zombie) {
        return isLeaderZombieStatic(zombie);
    }

    private static void giveGlowingEffect(LivingEntity entity) {
        int ticks = Integer.MAX_VALUE;
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false));
    }
}
