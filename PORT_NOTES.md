# SeeLeaderZombie — Multi-Loader Port Notes (MC 26.1, 26.1.1, 26.1.2, 26.2)

## Project Directory Matrix

| Directory | MC Version | Loader | Loader Version | Status |
|-----------|-----------|--------|----------------|--------|
| `SeeLeaderZombie-NeoForge-26.1` | 26.1 | NeoForge | 26.1.0.19-beta | Beta |
| `SeeLeaderZombie-NeoForge-26.1.1` | 26.1.1 | NeoForge | 26.1.1.15-beta | Beta |
| `SeeLeaderZombie-NeoForge-26.1.2` | 26.1.2 | NeoForge | 26.1.2.84 | **Stable** ✅ |
| `SeeLeaderZombie-NeoForge-26.2` | 26.2 | NeoForge | 26.2.0.32-beta | Beta |
| `SeeLeaderZombie-Fabric-26.1` | 26.1 | Fabric | 0.19.3 (API 0.145.1+26.1) | Stable |
| `SeeLeaderZombie-Fabric-26.1.1` | 26.1.1 | Fabric | 0.19.3 (API 0.155.2+26.1.1) | Stable |
| `SeeLeaderZombie-Fabric-26.1.2` | 26.1.2 | Fabric | 0.19.3 (API 0.155.2+26.1.2) | **Stable** ✅ |
| `SeeLeaderZombie-Fabric-26.2` | 26.2 | Fabric | 0.19.3 (API 0.155.2+26.2) | Stable |

## Dedicated Server & Singleplayer (Client) Compatibility

All projects are engineered for dual environment execution:

1. **Logical Side Safety (NeoForge)**:
   - `EntityJoinLevelEvent` checks `if (event.getLevel().isClientSide()) return;` so attribute inspection, entity healing, glowing effects, and entity removal ONLY occur on the server side (Integrated Server in Singleplayer or Dedicated Server in Multiplayer).
   - GUI configuration extension points are registered via `FMLEnvironment.dist == Dist.CLIENT` inside the constructor, preventing dedicated servers from crashing with `ClassNotFoundException` when loading client GUI classes.
   - `@Mod` annotation removed from `ConfigClient` to prevent duplicate mod initialization errors on server startup.

2. **Logical Side Safety (Fabric)**:
   - Uses `ServerLifecycleEvents.SERVER_STARTED` and `ServerEntityEvents.ENTITY_LOAD` from Fabric API.
   - Fabric API ensures these lifecycle events execute strictly on the Logical Server in both Singleplayer and Dedicated Server environments.
   - Config relies on standard `com.google.gson` saved to `.minecraft/config/seeleaderzombie.json`.

## How to Build

Navigate to any of the project directories and run Gradle build:

```bash
# NeoForge 26.1.2 (Recommended Stable)
cd SeeLeaderZombie-NeoForge-26.1.2
./gradlew build

# Fabric 26.1.2 (Recommended Stable)
cd SeeLeaderZombie-Fabric-26.1.2
./gradlew build
```

> **JDK Requirement:** Java 25.
