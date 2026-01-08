package net.robotic.seeleaderzombie;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = Seeleaderzombie.MODID)
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLE_LEADER_ZOMBIES =
            BUILDER.comment("Should the leader zombies be enabled in the first place? (default: true)")
                    .define("enableLeaderZombies", true);

    private static final ModConfigSpec.BooleanValue LOGGING =
            BUILDER.comment("Should logging of e.g. leader zombie spawn be enabled? (default: false)")
                    .define("logging", false);

    static final ModConfigSpec SPEC = BUILDER.build();

    public static boolean enableLeaderZombies;
    public static boolean logging;

    @SubscribeEvent
    static void onConfigLoaded(final ModConfigEvent.Loading event) {
        enableLeaderZombies = ENABLE_LEADER_ZOMBIES.get();
        logging = LOGGING.get();
    }

    @SubscribeEvent
    static void onConfigReloaded(final ModConfigEvent.Reloading event) {
        enableLeaderZombies = ENABLE_LEADER_ZOMBIES.get();
        logging = LOGGING.get();
    }
}
