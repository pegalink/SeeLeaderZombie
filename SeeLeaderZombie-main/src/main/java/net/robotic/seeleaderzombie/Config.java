package net.robotic.seeleaderzombie;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Seeleaderzombie.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_LEADER_ZOMBIES = BUILDER
            .comment("Should the leader zombies be enabled in the first place? (default: true)")
            .define("enableLeaderZombies", true);

    private static final ModConfigSpec.BooleanValue LOGGING = BUILDER
            .comment("Should logging of e.g. leader zombie spawn be enabled? (default: false)")
            .define("logging", false);

    private static final ModConfigSpec.BooleanValue HEAL_LEADER = BUILDER.comment(
            "Should the Leader Zombie be healed to its max health? (for versions 26.1 - 26.2) (default: true)")
            .define("healLeader", true);
    
    private static final ModConfigSpec.BooleanValue USE_NAME = BUILDER
            .comment("Instead of giving leader zombies a glowing effect, should they just be named? (default: false)")
            .define("useName", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableLeaderZombies;
    public static boolean logging;
    public static boolean healLeader;
    public static boolean useName;

    @SubscribeEvent
    static void onConfigLoaded(final ModConfigEvent.Loading event) {
        enableLeaderZombies = ENABLE_LEADER_ZOMBIES.get();
        logging = LOGGING.get();
        healLeader = HEAL_LEADER.get();
        useName = USE_NAME.get();
    }

    @SubscribeEvent
    static void onConfigReloaded(final ModConfigEvent.Reloading event) {
        enableLeaderZombies = ENABLE_LEADER_ZOMBIES.get();
        logging = LOGGING.get();
        healLeader = HEAL_LEADER.get();
        useName = USE_NAME.get();
    }
}