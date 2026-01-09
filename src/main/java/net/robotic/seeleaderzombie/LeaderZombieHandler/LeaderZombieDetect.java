package net.robotic.seeleaderzombie.LeaderZombieHandler;

import com.mojang.logging.LogUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.Drowned;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
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
                if ((entity instanceof Zombie || entity instanceof Drowned || entity instanceof ZombifiedPiglin) && isLeaderZombie((LivingEntity) entity)) {
                    if (!Config.enableLeaderZombies) {
                        entity.discard();
                        if (Config.logging) {
                            LOGGER.info("Removed a zombie leader due to configuration settings. Leader was at: \\{}", entity.blockPosition());
                        }
                    } else {
                        giveGlowingEffect((LivingEntity) entity);
                        if (Config.logging) {
                            LOGGER.info("Detected an existing leader zombie at server start at: \\{}", entity.blockPosition());
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
        LivingEntity entity = null;
        if (event.getEntity() instanceof Zombie zombie) {
            entity = zombie;
        } else if (event.getEntity() instanceof Drowned drowned) {
            entity = drowned;
        } else if (event.getEntity() instanceof ZombifiedPiglin zombifiedPiglin) {
            entity = zombifiedPiglin;
        }
        
        if (entity != null && isLeaderZombie(entity)) {
            if (!Config.enableLeaderZombies) {
                entity.discard();
                if (Config.logging) {
                    LOGGER.info("Removed a zombie leader due to configuration settings. Leader was spawned at: \\{}", entity.blockPosition());
                }
            } else {
                giveGlowingEffect(entity);
                if (Config.logging) {
                    LOGGER.info("Detected a leader zombie spawned at: \\{}", entity.blockPosition());
                }
            }
        }
    }

    public static boolean isLeaderZombieStatic(LivingEntity entity) {
        if (entity.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE) != null) {
            return Objects.requireNonNull(entity.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE)).getValue()
                    > LEADER_REINFORCEMENT_THRESHOLD;
        }
        return false;
    }

    public boolean isLeaderZombie(LivingEntity entity) {
        return isLeaderZombieStatic(entity);
    }

    private static void giveGlowingEffect(LivingEntity entity) {
        int ticks = Integer.MAX_VALUE;
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, ticks, 0, false, false));
    }
}
