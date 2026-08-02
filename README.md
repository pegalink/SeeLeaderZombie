# See Leader Zombies
Download this mod on modrinth!
link will be here soon

In the recent snapshot versions of 26.1, a long lasting [bug](https://bugs.mojang.com/browse/MC/issues/MC-219981) that causes the zombie leaders not have their maximum health. This has now been **fixed**, and this mod helps you reliably identify which of the zombies are leader zombies with a glowing effect.

## Characteristics of leader zombie
- They have increased health (more than double of the player)
- They have higher chances of calling in reinforcements

## Do I need this mod?
Depends. If you play easy or peaceful mode, this mod is useless to you. If you play normal or hard mode, it may be useful to identify leader zombies.

## Why did you make this mod?
Becasue some people like me like to know when I am fighting against a quite the strong mob. The leader zombie can have as much as **double the player's health**, it has **increased reinforcement chances** and even worse, **it can spawn as a baby with armor**!

## Supported versions

Both loaders are supported on every version, in singleplayer and on dedicated servers.

| Minecraft | NeoForge         | Fabric API      |
|-----------|------------------|-----------------|
| 26.1      | `26.1.0.19-beta` | `0.145.1+26.1`  |
| 26.1.1    | `26.1.1.15-beta` | `0.155.2+26.1.1`|
| 26.1.2    | `26.1.2.84`      | `0.155.2+26.1.2`|
| 26.2      | `26.2.0.32-beta` | `0.155.2+26.2`  |

## Configuration

| Option                | Default | Effect                                                       |
|-----------------------|---------|--------------------------------------------------------------|
| `enableLeaderZombies` | `true`  | When false, leader zombies are removed as they appear         |
| `logging`             | `false` | Log every detection and removal                               |
| `healLeader`          | `true`  | Heal a leader to full health when it loads                    |
| `useName`             | `false` | Mark leaders with a `Leader Zombie` name tag instead of a glow |

**NeoForge** reads `config/seeleaderzombie-common.toml` and has an in-game config screen in the
mod list. Changing the config applies to leaders that are already loaded, not just newly spawned
ones.

**Fabric** reads `config/seeleaderzombie.json` and gives the same control in-game through
commands (operators only):

```
/seeleaderzombie status               # show the current settings
/seeleaderzombie set useName true     # change a setting and apply it immediately
/seeleaderzombie reload               # re-read the config file from disk
```

Each of these re-scans the loaded world, so a change takes effect on the leaders already
around you.

## Building

One source tree builds every variant. `versions/<minecraft>.properties` is the only place
version numbers live.

```bash
./gradlew build                       # default Minecraft version (26.1.2), both loaders
./gradlew build -Pmc=26.2             # a specific version, both loaders
./gradlew :fabric:build -Pmc=26.1.1   # one loader, one version
./gradlew supportedVersions           # list what can be built
./build_all.sh                        # every loader x every version, jars gathered in builds/
```

Jars are named `seeleaderzombie-<loader>-<minecraft>-<mod version>.jar`, so all eight can sit in
one folder. Supporting a new Minecraft version means adding one file to `versions/` — no source
or build script changes. See [PORT_NOTES.md](PORT_NOTES.md) for the layout.

> **JDK requirement:** Java 25.

The code is open source, so you can modify it

# Should you find any issues? Open an issue on github!
