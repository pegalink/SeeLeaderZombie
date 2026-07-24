package net.robotic.seeleaderzombie.LeaderZombieHandler;

import com.mojang.logging.LogUtils;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.robotic.seeleaderzombie.utils.GetCurrentVersion;
import net.robotic.seeleaderzombie.utils.SetEntityName;
import net.robotic.seeleaderzombie.Config;
import org.slf4j.Logger;
import java.util.Objects;

public class LeaderZombieDetect {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double LEADER_REINFORCEMENT_THRESHOLD = 0.1;
    private static final String VERSION = new GetCurrentVersion().whichVersion();
    private static final String eName = "Leader Zombie";
    private static final Integer effectTime = Integer.MAX_VALUE;

    private static boolean isSupportedLeaderMob(LivingEntity entity) {
        EntityType<?> type = entity.getType();
        return type == EntityType.ZOMBIE || type == EntityType.DROWNED || type == EntityType.ZOMBIFIED_PIGLIN;
    }

    private static boolean is26xVersion() {
        return VERSION.equals("26.1") || VERSION.equals("26.1.1") || VERSION.equals("26.1.2") || VERSION.equals("26.2");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        event.getServer().getAllLevels().forEach(world -> {
            world.getAllEntities().forEach(entity -> {
                if (entity instanceof LivingEntity livingEntity
                        && isSupportedLeaderMob(livingEntity)
                        && isLeaderZombie(livingEntity)) {
                    if (!Config.enableLeaderZombies) {
                        entity.discard();
                        if (Config.logging) {
                            LOGGER.info("Removed a zombie leader due to configuration settings. Leader was at: {}", entity.blockPosition());
                        }
                    } else {
                        if (Config.useName) {
                            livingEntity.removeEffect(MobEffects.GLOWING);
                            SetEntityName.setEntityName(livingEntity, eName);
                        } else {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, effectTime, 0, false, false));
                            SetEntityName.setEntityName(livingEntity, null);
                        }
                        if (Config.logging) {
                            LOGGER.info("Detected an existing leader zombie at server start at: {}", entity.blockPosition());
                        }
                    }
                }
            });
        });
        if (Config.logging) {
            LOGGER.info("LeaderZombieDetect: checked existing zombies on server start.");
        }
        if (is26xVersion() && Config.healLeader) {
            LOGGER.warn("Existing leader zombies will not be healed despite healLeader being true! Only newly spawned leaders will be healed");
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(EntityJoinLevelEvent event) {
        // Ensure entity processing only occurs on the server side (dedicated server or integrated server)
        if (event.getLevel().isClientSide()) return;

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!isSupportedLeaderMob(entity)) return;

        if (isLeaderZombie(entity)) {
            if (!Config.enableLeaderZombies) {
                entity.discard();
                if (Config.logging) {
                    LOGGER.info("Removed a zombie leader due to configuration settings. Leader was spawned at: {}", entity.blockPosition());
                }
            } else {
                if (Config.useName) {
                    SetEntityName.setEntityName(entity, eName);
                } else {
                    giveGlowingEffect(entity);
                }
                if (is26xVersion() && Config.healLeader) {
                    entity.heal(entity.getMaxHealth());
                }
                if (Config.logging) {
                    LOGGER.info("Detected a leader zombie spawned at: {}", entity.blockPosition());
                    if (is26xVersion() && Config.healLeader) {
                        LOGGER.info("Leader zombie healed to max health on spawn.");
                    }
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
        entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, effectTime, 0, false, false));
    }
}
