package net.robotic.seeleaderzombie.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import net.fabricmc.loader.api.FabricLoader;
import net.robotic.seeleaderzombie.SeeLeaderZombieMod;
import net.robotic.seeleaderzombie.core.LeaderZombieSettings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JSON-backed configuration, stored at {@code config/seeleaderzombie.json}.
 *
 * <p>Option names and defaults deliberately mirror the NeoForge {@code ModConfigSpec} so a
 * player moving between loaders sees the same settings.
 */
public final class ModConfig implements LeaderZombieSettings {

    /** Shared view handed to the core logic. */
    public static final ModConfig INSTANCE = new ModConfig();

    /** The options that can be changed with {@code /seeleaderzombie set}. */
    public enum Option {
        ENABLE_LEADER_ZOMBIES("enableLeaderZombies"),
        LOGGING("logging"),
        HEAL_LEADER("healLeader"),
        USE_NAME("useName");

        private final String key;

        Option(String key) {
            this.key = key;
        }

        /** The name used in the config file and as the command literal, so the two always match. */
        public String key() {
            return key;
        }
    }

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("seeleaderzombie.json");

    private static volatile ConfigData data = new ConfigData();

    private ModConfig() {
    }

    /** Mutable shape written to and read from disk. Field names are the config keys. */
    private static final class ConfigData {
        boolean enableLeaderZombies = true;
        boolean logging = false;
        boolean healLeader = true;
        boolean useName = false;

        ConfigData copy() {
            ConfigData other = new ConfigData();
            other.enableLeaderZombies = enableLeaderZombies;
            other.logging = logging;
            other.healLeader = healLeader;
            other.useName = useName;
            return other;
        }
    }

    /**
     * Reads the config from disk, writing a default file if none exists.
     *
     * @return true if the file was read successfully (or freshly created)
     */
    public static boolean load() {
        if (!Files.exists(CONFIG_PATH)) {
            save();
            return true;
        }
        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            ConfigData loaded = GSON.fromJson(reader, ConfigData.class);
            if (loaded != null) {
                data = loaded;
            }
            // An older config file may be missing newly added keys; rewrite it so they show up.
            save();
            return true;
        } catch (IOException | JsonSyntaxException e) {
            SeeLeaderZombieMod.LOGGER.error("Failed to read {}, keeping current values", CONFIG_PATH, e);
            return false;
        }
    }

    public static void save() {
        ConfigData snapshot = data.copy();
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(snapshot, writer);
            }
        } catch (IOException e) {
            SeeLeaderZombieMod.LOGGER.error("Failed to write {}", CONFIG_PATH, e);
        }
    }

    public static boolean get(Option option) {
        ConfigData current = data;
        return switch (option) {
            case ENABLE_LEADER_ZOMBIES -> current.enableLeaderZombies;
            case LOGGING -> current.logging;
            case HEAL_LEADER -> current.healLeader;
            case USE_NAME -> current.useName;
        };
    }

    /** Sets an option and persists it. */
    public static void set(Option option, boolean value) {
        ConfigData updated = data.copy();
        switch (option) {
            case ENABLE_LEADER_ZOMBIES -> updated.enableLeaderZombies = value;
            case LOGGING -> updated.logging = value;
            case HEAL_LEADER -> updated.healLeader = value;
            case USE_NAME -> updated.useName = value;
        }
        data = updated;
        save();
    }

    public static Path path() {
        return CONFIG_PATH;
    }

    @Override
    public boolean enableLeaderZombies() {
        return data.enableLeaderZombies;
    }

    @Override
    public boolean logging() {
        return data.logging;
    }

    @Override
    public boolean healLeader() {
        return data.healLeader;
    }

    @Override
    public boolean useName() {
        return data.useName;
    }
}
