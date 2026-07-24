package net.robotic.seeleaderzombie.handler;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.robotic.seeleaderzombie.SeeLeaderZombieMod;
import net.robotic.seeleaderzombie.config.ModConfig;

import java.util.Objects;

/**
 * Handles leader zombie detection and modification.
 * Equivalent to the NeoForge LeaderZombieDetect + LeaderZombieConfigSync classes.
 */
public class LeaderZombieHandler {
    private static final double LEADER_REINFORCEMENT_THRESHOLD = 0.1;
    private static final String ENTITY_NAME = "Leader Zombie";
    private static final int EFFECT_DURATION = Integer.MAX_VALUE;

    /**
     * Called when the server starts - scan all existing entities.
     */
    public static void onServerStart(MinecraftServer server) {
        server.getAllLevels().forEach(level -> {
            level.getAllEntities().forEach(entity -> {
                if (entity instanceof LivingEntity living && isSupportedLeaderMob(living) && isLeaderZombie(living)) {
                    if (!ModConfig.enableLeaderZombies) {
                        entity.discard();
                        if (ModConfig.logging) {
                            SeeLeaderZombieMod.LOGGER.info("Removed leader zombie at {} (enableLeaderZombies=false)", entity.blockPosition());
                        }
                    } else {
                        applyLeaderEffect(living);
                        if (ModConfig.logging) {
                            SeeLeaderZombieMod.LOGGER.info("Detected existing leader zombie at {}", entity.blockPosition());
                        }
                    }
                }
            });
        });
        if (ModConfig.logging) {
            SeeLeaderZombieMod.LOGGER.info("SeeLeaderZombie: finished scanning existing entities on server start.");
        }
    }

    /**
     * Called when an entity loads into a world level.
     */
    public static void onEntityLoad(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        if (!isSupportedLeaderMob(living)) return;
        if (!isLeaderZombie(living)) return;

        if (!ModConfig.enableLeaderZombies) {
            entity.discard();
            if (ModConfig.logging) {
                SeeLeaderZombieMod.LOGGER.info("Removed leader zombie at {} (enableLeaderZombies=false)", entity.blockPosition());
            }
            return;
        }

        applyLeaderEffect(living);

        if (ModConfig.healLeader) {
            living.heal(living.getMaxHealth());
        }

        if (ModConfig.logging) {
            SeeLeaderZombieMod.LOGGER.info("Leader zombie spawned at {}", entity.blockPosition());
            if (ModConfig.healLeader) {
                SeeLeaderZombieMod.LOGGER.info("Leader zombie healed to max health.");
            }
        }
    }

    /**
     * Applies the configured visual effect (glow or name tag) to a leader zombie.
     */
    private static void applyLeaderEffect(LivingEntity entity) {
        if (ModConfig.useName) {
            entity.removeEffect(MobEffects.GLOWING);
            entity.setCustomName(net.minecraft.network.chat.Component.literal(ENTITY_NAME));
            entity.setCustomNameVisible(true);
        } else {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, EFFECT_DURATION, 0, false, false));
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }
    }

    /**
     * Returns true if the entity type is one that can be a leader zombie.
     */
    public static boolean isSupportedLeaderMob(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.ZOMBIE || type == EntityType.DROWNED || type == EntityType.ZOMBIFIED_PIGLIN;
    }

    /**
     * Returns true if the entity has a reinforcement chance > threshold (i.e. is a "leader").
     */
    public static boolean isLeaderZombie(LivingEntity entity) {
        var attr = entity.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        if (attr == null) return false;
        return Objects.requireNonNull(attr).getValue() > LEADER_REINFORCEMENT_THRESHOLD;
    }
}
