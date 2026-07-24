package net.robotic.seeleaderzombie.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.robotic.seeleaderzombie.SeeLeaderZombieMod;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Simple JSON-based configuration for the Fabric port.
 * Config file: config/seeleaderzombie.json
 */
public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("seeleaderzombie.json");

    // Config values (with defaults)
    public static boolean enableLeaderZombies = true;
    public static boolean logging = false;
    public static boolean healLeader = true;
    public static boolean useName = false;

    /** Internal data class for JSON serialization */
    private static class ConfigData {
        boolean enableLeaderZombies = true;
        boolean logging = false;
        boolean healLeader = true;
        boolean useName = false;
    }

    public static void load() {
        if (Files.exists(CONFIG_PATH)) {
            try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                if (data != null) {
                    enableLeaderZombies = data.enableLeaderZombies;
                    logging = data.logging;
                    healLeader = data.healLeader;
                    useName = data.useName;
                }
            } catch (IOException e) {
                SeeLeaderZombieMod.LOGGER.error("Failed to load config, using defaults", e);
            }
        } else {
            save(); // Write default config
        }
    }

    public static void save() {
        ConfigData data = new ConfigData();
        data.enableLeaderZombies = enableLeaderZombies;
        data.logging = logging;
        data.healLeader = healLeader;
        data.useName = useName;

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException e) {
            SeeLeaderZombieMod.LOGGER.error("Failed to save config", e);
        }
    }
}
