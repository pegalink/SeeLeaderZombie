# SeeLeaderZombie — Multi-Loader Notes (MC 26.1, 26.1.1, 26.1.2, 26.2)

## Layout

There is one copy of the sources. Minecraft versions are a *build input*, not a directory.

```
common/    loader-agnostic logic, compiled into both jars
neoforge/  NeoForge entry point, config spec, event adapters
fabric/    Fabric entry point, JSON config, commands
versions/  one .properties file per supported Minecraft version
```

`versions/<minecraft>.properties` is the single source of truth for every per-version
coordinate: the Minecraft version, the NeoForge version and range, the Fabric loader/API
versions, and the dependency ranges written into `neoforge.mods.toml` and `fabric.mod.json`.
The root `build.gradle` loads the selected file and injects its keys into both subprojects, so
**supporting a new Minecraft version means adding one file** — no source or build script edits.

| Minecraft | NeoForge         | Status | Fabric API       | Status |
|-----------|------------------|--------|------------------|--------|
| 26.1      | `26.1.0.19-beta` | Beta   | `0.145.1+26.1`   | Stable |
| 26.1.1    | `26.1.1.15-beta` | Beta   | `0.155.2+26.1.1` | Stable |
| 26.1.2    | `26.1.2.84`      | Stable | `0.155.2+26.1.2` | Stable |
| 26.2      | `26.2.0.32-beta` | Beta   | `0.155.2+26.2`   | Stable |

`26.1.2` is the default build target and the recommended release.

## What lives where

`common/` holds `LeaderZombies`, which contains all of the behaviour: identifying a leader,
marking it, healing it, discarding it, and re-syncing an already-loaded world. It reads its
settings through the `LeaderZombieSettings` interface and logs through SLF4J, so it references
no loader types at all.

Each loader module is a thin adapter:

| Concern              | NeoForge                                       | Fabric                                     |
|----------------------|------------------------------------------------|--------------------------------------------|
| Entry point          | `Seeleaderzombie` (`@Mod`)                     | `SeeLeaderZombieMod` (`ModInitializer`)    |
| Config storage       | `ModConfigSpec` → `seeleaderzombie-common.toml`| GSON → `config/seeleaderzombie.json`       |
| Entity load hook     | `EntityJoinLevelEvent`                         | `ServerEntityEvents.ENTITY_LOAD`           |
| World scan hook      | `ServerStartedEvent`                           | `ServerLifecycleEvents.SERVER_STARTED`     |
| Applying a live edit | `ModConfigEvent.Reloading`                     | `/seeleaderzombie reload` and `set`        |
| Config UI            | `IConfigScreenFactory` (client only)           | commands                                   |

Because both loaders call the same code, they cannot drift apart in behaviour.

## Dedicated server and singleplayer

Both builds run on a dedicated server and in singleplayer.

- **NeoForge** — `EntityJoinLevelEvent` returns early when `event.getLevel().isClientSide()`, so
  attribute inspection, healing, glowing and removal only happen on the logical server. The
  config screen hook is isolated in `client/ClientConfigScreen`, which is only referenced inside
  a `FMLEnvironment.getDist() == Dist.CLIENT` branch; a dedicated server never loads the class,
  so its client-only GUI types are never resolved.
- **Fabric** — `ServerLifecycleEvents.SERVER_STARTED` and `ServerEntityEvents.ENTITY_LOAD` are
  fired on the logical server in both environments by Fabric API. There are no client-side
  entrypoints, and `environment` is `*` so the mod also loads for singleplayer's integrated
  server.

## Behaviour changes made during consolidation

These were fixed while merging the eight copies; they applied to some or all of them.

- **NeoForge start-up scan never ran.** It was hooked to `ServerStartingEvent`, which fires
  before the levels exist, so `server.getAllLevels()` was empty. Now `ServerStartedEvent`,
  matching what Fabric already did.
- **Removal during iteration.** Leaders were `discard()`ed while iterating the level's live
  entity view. They are now collected first, then acted on.
- **Fabric had no way to apply a config change.** Editing the JSON only affected leaders that
  spawned afterwards. `/seeleaderzombie reload|set|status` now re-syncs the loaded world, which
  is what NeoForge's `ModConfigEvent.Reloading` handler does.
- **Config read races.** The NeoForge reload handler and the config-value refresh were separate
  listeners with no ordering guarantee, so the re-sync could read stale values. The handler now
  refreshes first.
- **Player name tags were erased.** With `useName=false` the mod cleared *any* custom name on a
  leader. It now only clears the name it applied itself.
- **Broken log placeholders.** The 26.2 copies used `\\{}` in SLF4J format strings, which prints
  a literal `{}` instead of the position.
- **Version-gated healing removed.** `healLeader` was gated behind a string comparison against
  `SharedConstants.getCurrentVersion().toString()` listing "26.1", "26.1.2", "26.2". That set
  omitted 26.1.1 in several copies, and depended on a `toString()` that is not contractually the
  version name. Every supported version is in range, so the gate only ever risked silently
  disabling the option; `healLeader` is now honoured directly.
- **Entity type matching unified.** Some copies compared `EntityType` constants, others matched
  registry paths. All now match registry paths (`zombie`, `drowned`, `zombified_piglin`), which
  behaves identically and does not depend on the constants keeping their names.

## Fabric build fixes

The Fabric variants were excluded from CI because they did not build. The causes:

- **The Gradle wrapper was too old.** Fabric Loom 1.17.x declares
  `org.gradle.plugin.api-version` 9.5.0, and the wrapper pinned Gradle 9.2.1, so Loom could not
  be resolved at all:

  ```
  No matching variant of net.fabricmc:fabric-loom:1.17.17 was found ...
    - Variant 'runtimeElements' ... attribute 'org.gradle.plugin.api-version' with value '9.5.0'
      and the consumer needed ... value '9.2.1'
  ```

  The wrapper is on 9.6.1 now, with the distribution checksum pinned.
- `org.gradle.configuration-cache=true` — Loom does not support Gradle's configuration cache.
  It is off at the root now.
- `loom { splitEnvironmentSourceSets() }` with no `src/client` source set, while registering
  only `sourceSets.main` for the mod. This mod has no client code, so the split is gone.
- `fabricApi { configureDataGeneration() }` with no datagen entrypoint. Removed.
- No `org.gradle.toolchains.foojay-resolver-convention` in the Fabric `settings.gradle`, so the
  Java 25 toolchain could not be provisioned. The root `settings.gradle` applies it for both.
- `publishing` referenced an `archives_base_name` property that only existed in the Fabric
  copies. Publications now use `base.archivesName`.

## Building

```bash
./gradlew build                       # default version (26.1.2), both loaders
./gradlew build -Pmc=26.2             # a specific version, both loaders
./gradlew :neoforge:build -Pmc=26.1 -Ploaders=neoforge   # one loader, one version
./gradlew supportedVersions           # list supported versions
./build_all.sh                        # all 8 variants -> builds/
./build_all.sh --loader fabric        # one loader, every version
```

Loader plugin versions (`moddev_version`, `loom_version`) live in the root `gradle.properties`
and can be overridden per invocation, e.g. `-Ploom_version=1.17.17`. The wrapper must stay at
Gradle 9.5.0 or newer for Loom 1.17.x.

`-Ploaders=<loader>` controls which loader projects are included in the build. Gradle configures
every included project, so without it a NeoForge-only build still has to resolve the Fabric Loom
plugin — which is how the Loom/Gradle version mismatch above failed the NeoForge jobs as well as
the Fabric ones. `build_all.sh` and each CI matrix job pass it, so the eight combinations are
genuinely independent; combined with `fail-fast: false`, one broken combination cannot hide the
state of the others.

> **JDK Requirement:** Java 25.
