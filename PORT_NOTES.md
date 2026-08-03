# SeeLeaderZombie — Multi-Loader Notes (MC 26.1 – 26.3-snapshot-6)

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
versions, the dependency ranges written into `neoforge.mods.toml` and `fabric.mod.json`, and a
`loaders=` key naming which loaders publish builds for that version. `settings.gradle` reads the
selected file and includes only those loader projects; `build.gradle` injects the rest of the
keys. CI derives its matrix from the same directory. So **supporting a new Minecraft version
means adding one file** — no source, build script or workflow edits, even when only one loader
has that version.

| Minecraft       | NeoForge         | Status | Fabric API       | Status   |
|-----------------|------------------|--------|------------------|----------|
| 26.1            | `26.1.0.19-beta` | Beta   | `0.145.1+26.1`   | Stable   |
| 26.1.1          | `26.1.1.15-beta` | Beta   | `0.145.4+26.1.1` | Stable   |
| 26.1.2          | `26.1.2.84`      | Stable | `0.155.2+26.1.2` | Stable   |
| 26.2            | `26.2.0.32-beta` | Beta   | `0.155.2+26.2`   | Stable   |
| 26.3-snapshot-6 | —                | —      | `0.156.1+26.3`   | Snapshot |

`26.1.2` is the default build target and the recommended release.

### 26.3-snapshot-6 is Fabric only

NeoForge publishes no builds for Minecraft snapshots, so `versions/26.3-snapshot-6.properties`
carries no `neo_*` coordinates and sets `loaders=fabric`. The NeoForge project is then not part
of the build when that version is selected, and asking for it anyway fails clearly:

```
> neoforge does not support Minecraft 26.3-snapshot-6. That version builds for: fabric.
```

Two details worth knowing about the Fabric coordinates. The Minecraft artifact really is
`26.3-snapshot-6`, while Fabric API releases against it as `+26.3` — the published artifact is
titled "[26.3-snapshot-6] Fabric API 0.156.1+26.3", so the two strings legitimately differ. And
`fabric.mod.json` pins that exact snapshot instead of a range, because snapshot names do not
order the way release versions do and a later snapshot can change the API again.

When NeoForge ships 26.3, add its coordinates to that file and put `neoforge` back in `loaders`;
nothing else needs to change.

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
./gradlew build                       # default version (26.1.2), its loaders
./gradlew build -Pmc=26.2             # a specific version
./gradlew build -Pmc=26.3-snapshot-6  # Fabric only; the NeoForge project is not included
./gradlew :neoforge:build -Pmc=26.1 -Ploaders=neoforge   # one loader, one version
./gradlew supportedVersions           # list versions and their loaders
./build_all.sh                        # every supported pair -> builds/
./build_all.sh --loader fabric        # one loader, every version that has it
```

Loader plugin versions (`moddev_version`, `loom_version`) live in the root `gradle.properties`
and can be overridden per invocation, e.g. `-Ploom_version=1.17.17`. The wrapper must stay at
Gradle 9.5.0 or newer for Loom 1.17.x.

`-Ploaders=<loader>` controls which loader projects are included in the build; it defaults to
whatever the selected version's `loaders=` key lists. Gradle configures every included project,
so without narrowing it a NeoForge-only build still has to resolve the Fabric Loom plugin —
which is how the Loom/Gradle version mismatch above failed the NeoForge jobs as well as the
Fabric ones. `build_all.sh` and each CI matrix job pass it, so the combinations are genuinely
independent; combined with `fail-fast: false`, one broken combination cannot hide the state of
the others.

`build_all.sh` skips loader/version pairs that do not exist rather than failing on them, and the
CI matrix is generated from `versions/` by its own job, so neither needs editing when a version
is added.

> **JDK Requirement:** Java 25.
