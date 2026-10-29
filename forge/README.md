# Forge 1.20.1

This platform shares the root `VERSION`, `common/` behaviors and resource catalog with NeoForge. Platform-specific code and native 3.31 differences live here. Generated Java is a build output; edit `generate_platform.py` or an explicit `src/main/java` override.

## Build

Use Java 17 and Python 3.9+. Keep Ars Conflux checked out beside Cataclysm-Nouveau (or set `-PconfluxCheckout=<path to Core/forge>`). The development baseline is Core `b46be94b17799573b825a8e4a11870fa3dc857d6` (0.1.29); do not update its sources as part of an addon build.

Place the actual published dependencies in ignored `forge/libs/`, using the filenames and versions in `dependencies.lock.json`. The lock records SHA-256 hashes; no native mod binary or media is bundled with this project.

```powershell
./gradlew.bat :runGameTestServer :jar :sourcesJar :verifyDistributions --offline --no-daemon --max-workers=1
```

`jar` is followed by `reobfJar`; distribute the final `*-mc1.20.1-forge.jar`. `verifyDistributions` checks both binary and source archives, including the no-bundled-Cataclysm-assets policy, license files, refmap and all 102 glyph recipes.

From 0.1.3, `verifyDistributions` also depends on `verifyProductionMixins`: a build-only ASM reader checks the final binary's method selectors, invocation sites, invokers/accessors and shadow fields against the **original production** dependencies and Forge's SRG Minecraft JAR. It does not load Minecraft or replace a client launch test. ASM/Gson are verification-tool dependencies only and are not bundled. Intentionally optional `require=0` misses in multi-target adapters are recorded separately.

Compilation declares the raw refmap, archive refmap copy and additional TSRG file as outputs, so `--build-cache` restores all three. A clean cache-restore build is part of the 0.1.3 regression check.

## Native differences

- Cataclysm 3.31 uses a living-entity tentacle capability rather than a data attachment.
- Ignis fireballs use vanilla `AbstractHurtingProjectile`. Checks remain restricted to the Ignis marker interface, so guardian bullets are not treated as Ignis fireballs.
- Elemental spears and cursed sandstorms own their native tick bodies. Controlled Ars trajectories retain native visual ticks while Conflux drives movement and collision.
- Cursed sandstorm uses its 3.31 lifetime/state and collision rules. Repeating carriers reset its ascending lifetime instead of applying the newer countdown semantics.
- Newer explosion-calculator entity hooks are bridged only for this addon's opted-in explosions. Forge's native explosion and Ignis's native explosion bodies still run; allies are removed from the detonation list and damage augmentation is applied at the actual native hit.
- Ars 4 has no Ars 5 per-parent augment description map. Compatibility and availability rules remain, while descriptions use Ars 4's normal presentation.
- Native 3D item icons need the native item stack outside GUI rendering. GUI framing remains owned by Conflux.

Use Core 0.1.30 for the corrected unlit GUI frames. The separate Core rendering fix is recorded in `../docs/forge-1.20.1-port.md`.

Automated server checks do not establish that every client particle or phantom animation has been visually inspected.
