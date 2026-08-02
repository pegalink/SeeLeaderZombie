package net.robotic.seeleaderzombie.core;

/**
 * Read-only view of the mod configuration.
 *
 * <p>Each loader backs this with its own config system (NeoForge's {@code ModConfigSpec},
 * Fabric's JSON file) so the shared logic in {@link LeaderZombies} never has to know which
 * loader it is running under.
 */
public interface LeaderZombieSettings {

    /** Whether leader zombies are allowed to exist at all. When false, they are discarded on sight. */
    boolean enableLeaderZombies();

    /** Whether detections and removals are written to the log. */
    boolean logging();

    /** Whether a leader is healed to full health when it joins a level. */
    boolean healLeader();

    /** Whether leaders are marked with a name tag instead of the glowing effect. */
    boolean useName();
}
