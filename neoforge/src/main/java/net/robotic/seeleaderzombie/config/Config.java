package net.robotic.seeleaderzombie.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.robotic.seeleaderzombie.core.LeaderZombieSettings;
import net.robotic.seeleaderzombie.core.LeaderZombies;

/**
 * NeoForge-backed configuration. Option names match the Fabric JSON config so both loaders
 * document and behave the same way.
 */
@EventBusSubscriber(modid = LeaderZombies.MOD_ID)
public final class Config implements LeaderZombieSettings {

    /** Shared view handed to {@link LeaderZombies}. */
    public static final Config INSTANCE = new Config();

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_LEADER_ZOMBIES = BUILDER
            .comment("Should the leader zombies be enabled in the first place? (default: true)")
            .define("enableLeaderZombies", true);

    private static final ModConfigSpec.BooleanValue LOGGING = BUILDER
            .comment("Should logging of e.g. leader zombie spawn be enabled? (default: false)")
            .define("logging", false);

    private static final ModConfigSpec.BooleanValue HEAL_LEADER = BUILDER
            .comment("Should the Leader Zombie be healed to its max health when it loads? (default: true)")
            .define("healLeader", true);

    private static final ModConfigSpec.BooleanValue USE_NAME = BUILDER
            .comment("Instead of giving leader zombies a glowing effect, should they just be named? (default: false)")
            .define("useName", false);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static volatile boolean enableLeaderZombies = true;
    private static volatile boolean logging = false;
    private static volatile boolean healLeader = true;
    private static volatile boolean useName = false;

    private Config() {
    }

    @SubscribeEvent
    static void onConfigLoaded(final ModConfigEvent.Loading event) {
        refresh();
    }

    @SubscribeEvent
    static void onConfigReloaded(final ModConfigEvent.Reloading event) {
        refresh();
    }

    /** Copies the spec values into the cached fields. Safe to call more than once. */
    public static void refresh() {
        enableLeaderZombies = ENABLE_LEADER_ZOMBIES.get();
        logging = LOGGING.get();
        healLeader = HEAL_LEADER.get();
        useName = USE_NAME.get();
    }

    @Override
    public boolean enableLeaderZombies() {
        return enableLeaderZombies;
    }

    @Override
    public boolean logging() {
        return logging;
    }

    @Override
    public boolean healLeader() {
        return healLeader;
    }

    @Override
    public boolean useName() {
        return useName;
    }
}
