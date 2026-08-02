package net.robotic.seeleaderzombie.core;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * All of the mod's actual behaviour, free of loader-specific types.
 *
 * <p>The NeoForge and Fabric modules are thin adapters: they translate their own lifecycle
 * events into calls on this class, so both loaders behave identically by construction.
 */
public final class LeaderZombies {

    public static final String MOD_ID = "seeleaderzombie";

    /** The name given to leaders when {@link LeaderZombieSettings#useName()} is enabled. */
    public static final String LEADER_NAME = "Leader Zombie";

    /**
     * Vanilla gives regular zombies a reinforcement chance well below this, and leaders a
     * chance well above it, so it cleanly separates the two on every difficulty.
     */
    private static final double LEADER_REINFORCEMENT_THRESHOLD = 0.1;

    /** Effectively permanent; the effect is re-applied whenever the entity is reloaded anyway. */
    private static final int GLOW_DURATION_TICKS = Integer.MAX_VALUE;

    /** Mobs that vanilla can spawn as reinforcement leaders. Matched by registry path so any namespace works. */
    private static final Set<String> LEADER_CAPABLE_MOBS = Set.of("zombie", "drowned", "zombified_piglin");

    private LeaderZombies() {
    }

    /** True if this entity is a mob type that vanilla can spawn as a leader. */
    public static boolean isLeaderCapable(LivingEntity entity) {
        ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return key != null && LEADER_CAPABLE_MOBS.contains(key.getPath());
    }

    /** True if this entity carries the boosted reinforcement chance that identifies a leader. */
    public static boolean isLeader(LivingEntity entity) {
        var attribute = entity.getAttribute(Attributes.SPAWN_REINFORCEMENTS_CHANCE);
        return attribute != null && attribute.getValue() > LEADER_REINFORCEMENT_THRESHOLD;
    }

    private static boolean isLeaderZombie(Entity entity) {
        return entity instanceof LivingEntity living && isLeaderCapable(living) && isLeader(living);
    }

    /**
     * Handles an entity that has just joined a server level.
     *
     * <p>Callers must have already filtered out the client side; this method assumes it is
     * running on the logical server.
     */
    public static void onEntityJoinedLevel(Entity entity, LeaderZombieSettings settings, Logger logger) {
        if (!isLeaderZombie(entity)) {
            return;
        }
        LivingEntity leader = (LivingEntity) entity;

        if (!settings.enableLeaderZombies()) {
            leader.discard();
            if (settings.logging()) {
                logger.info("Removed a leader zombie that spawned at {} (enableLeaderZombies=false)", leader.blockPosition());
            }
            return;
        }

        applyMarker(leader, settings);

        if (settings.healLeader()) {
            leader.heal(leader.getMaxHealth());
        }

        if (settings.logging()) {
            logger.info("Detected a leader zombie at {}{}", leader.blockPosition(),
                    settings.healLeader() ? " (healed to max health)" : "");
        }
    }

    /**
     * Brings every already-loaded entity in line with the current settings.
     *
     * <p>Used both when a server finishes starting and when the config is reloaded while the
     * server is running, so a config change takes effect on existing mobs rather than only on
     * newly spawned ones.
     *
     * @return the number of leaders that were discarded or re-marked
     */
    public static int resyncLoadedEntities(MinecraftServer server, LeaderZombieSettings settings, Logger logger) {
        int discarded = 0;
        int marked = 0;

        for (ServerLevel level : server.getAllLevels()) {
            // Collect first: discarding while iterating the level's entity view can fail.
            List<LivingEntity> leaders = new ArrayList<>();
            for (Entity entity : level.getAllEntities()) {
                if (isLeaderZombie(entity)) {
                    leaders.add((LivingEntity) entity);
                }
            }

            for (LivingEntity leader : leaders) {
                if (!settings.enableLeaderZombies()) {
                    leader.discard();
                    discarded++;
                } else {
                    applyMarker(leader, settings);
                    marked++;
                }
            }
        }

        if (settings.logging()) {
            logger.info("Re-synced loaded entities: {} leader zombie(s) marked, {} removed", marked, discarded);
        }
        return discarded + marked;
    }

    /**
     * Marks a leader according to the current settings: either a name tag or the glowing effect,
     * never both.
     */
    public static void applyMarker(LivingEntity entity, LeaderZombieSettings settings) {
        if (settings.useName()) {
            entity.removeEffect(MobEffects.GLOWING);
            entity.setCustomName(Component.literal(LEADER_NAME));
            entity.setCustomNameVisible(true);
        } else {
            // Only clear a name this mod applied - a player's own name tag is left alone.
            if (hasOurName(entity)) {
                entity.setCustomName(null);
                entity.setCustomNameVisible(false);
            }
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOW_DURATION_TICKS, 0, false, false));
        }
    }

    private static boolean hasOurName(LivingEntity entity) {
        Component name = entity.getCustomName();
        return name != null && LEADER_NAME.equals(name.getString());
    }
}
