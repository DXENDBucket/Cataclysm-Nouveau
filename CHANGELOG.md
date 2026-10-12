# Changelog

## 0.1.0-dev.32

- Add an optional client-side translucent Ignis Sword Dance phantom, using the native renderer, model, textures and both animation phases.
- Sync actual executor identity, attack direction, phase and progress through small four-tick snapshots. Normal completion and cancellation close the visual; removed actors use a separate nearby-observer close path instead of relying on their tracking entry.
- Blend all textured native layers without phantom depth writes, keep native scale by default, and lower opacity for the local first-person executor. Client configuration can disable or adjust the visual.
- Never spawn a real Boss or run a proxy's tick, AI, damage or movement. Mana, cooldowns and existing gameplay stay unchanged. All 77 required server tests and distribution checks pass; client graphics await manual verification.

## 0.1.0-dev.31

- Complete the selected Ignis series with Five-Fireball Array, Earth Shudders, Reinforced Leaping Smash, Ultimate Flame Slash and standalone Flame Strike effect glyphs.
- Invoke native goals and attack bodies: staggered mixed salvo, three ground strikes, both actual-landing variants and their six total fireballs, the ultimate preparation ring, eight forward fields and broad shockwaves.
- Keep Flame Strike's native synced visibility, waiting period, pulses, Blazing Brand, shrinking, ending particles and Boss-radius explosion; soul fields do not explode. Standalone fields support time augments.
- Extend health-damage caps and Ars scaling to flame fields, keep real ownership and allied damage/motion protection, and clean all delayed children through Conflux sessions. Native Cataclysm casts remain unchanged.
- Add configurable fees/cooldowns, bilingual descriptions, runtime-only icons and recipes. Split one Ignitium Ingot into nine fragments for the two basic fireball glyphs; nine ingots cover all ten glyphs, within three fixed-loot kills. Existing Mana costs and cooldowns are unchanged.
- All 75 required server tests and binary/source distribution checks pass with MixinExtras 0.5.3. Client visuals remain unverified.

## 0.1.0-dev.30

- Add default-enabled `damage.cap_health_damage`: otherwise uncapped maximum-health damage in addon casts cannot exceed the fixed damage of the same hit, before damage augments and Ars scaling.
- Cover Ignis ordinary/soul/Abyss fireballs, prepared child shots, native melee and ground shockwaves, and Scylla lightning storms including child strikes. Keep original Cataclysm attacks, explosions and pre-existing native caps unchanged.
- Keep all Mana costs and cooldowns unchanged. All 69 required server tests pass, including the disabled configuration, low-health targets and native unbound attacks.

## 0.1.0-dev.29

- Add native Ignis Fireball and Abyss Fireball replacements, Blade Fireball salvo, Leaping Smash and the complete two-phase Sword Dance.
- Keep native trail history, explosions and particle packets, Abyss wall bounces, health-based damage, Blazing Brand and healing. Prepared shots acquire the selected target even with a real player owner.
- Use original two-thirds/one-third health gates for soul fire and shield-break state. Jump damage waits for real landing; ordinary smash does not ignite.
- Preserve explicit cast ownership during deflection, ally protection, terrain, trigger billing, actor/child lifetimes and conflict cleanup. Ordinary Cataclysm behavior stays unchanged.
- Add five ingot-based recipes, runtime-only models, native item-render delegation, descriptions and configurable costs/cooldowns. All 67 required server tests and binary/source distribution checks pass; client visuals remain unverified.

## 0.1.0-dev.28

- Add Guardian Uppercut Combo, Blink Strike, Aerial Slam and Rocket Punch as effect glyphs, using complete native goals and attack bodies.
- Preserve the nine prepared bullets, half-health uppercut follow-up/rune fan, predicted blink, and actual-landing shockwave/radial runes. Real actors move; detached Boss frames are never spawned or broadcast as animation entities.
- Honor teleport cancellation, clear space, loaded chunks, real damage ownership, allies, trigger fees and cancellation cleanup. Prevent conflicting movement actions.
- Four fragment-based Scribe recipes, runtime-reference icons, bilingual text, Boss-series ordering and configurable Mana/cooldowns. All 61 required server tests and binary/source distribution checks pass; client presentation remains unverified.

## 0.1.0-dev.27

- Disassemble a Gauntlet of Guard into nine Void Gauntlet Fragments.
- Require a fragment for Guardian Stomp as well as Vortex and Gravity Punch. Keep golem Void Cores for the weaker standalone Void Rune and Guardian Bullet.

## 0.1.0-dev.26

- Fix managed Void Vortex client replicas being discarded on their first tick in integrated-server games. Check caster death/world changes only on the server.
- Apply the same server boundary to Wither Smoke and Guardian Rune root cleanup. Native client visuals and server expiry/root-loss cleanup remain in place.
- Disassemble one Gauntlet of Guard into four Void Gauntlet Fragments. Vortex and Gravity Punch each consume one fragment, instead of a whole gauntlet; keep their glyph icons and Void Core recipes.
- The dev.25 client capture showed three first-tick removals with 300 ticks remaining and normal culling metadata; this was a lifecycle error, rather than evidence of optimizer culling.

## 0.1.0-dev.25

- Add default-off, bounded client Void Vortex diagnostics: actual client lifetime, managed culling metadata, native renderer/quad calls, selected textures and resource-pack provenance.
- Record ordinary Boss vortices as well as glyph vortices for comparison. No rendering, lifetime or optimizer settings are changed.
- The dev.24 culling protection did not resolve the player's disappearing-image report; the cause remains unconfirmed.

## 0.1.0-dev.24

- Synchronize culling protection for managed Void Vortices, and describe their full native visual extent separately from the physical box. Keep the native renderer, particles, origin, lifespan and damage behavior.
- Verify that receiving spawn metadata installs the protection; ordinary Cataclysm vortices keep their original culling rules. Client confirmation remains pending.

## 0.1.0-dev.23

- Add Guardian Bullet, Void Vortex, Guardian Stomp, and Gravity Punch with native preparation, control, rune patterns and attack bodies. Include the half-health triple stomp.
- Reuse native-projectile adaptation for ordinary and Ars-driven trajectories. Preserve real damage ownership, ally filtering, trigger billing, cleanup and bounded damage augments.
- Add real Scribe recipes, bilingual descriptions, runtime-reference icons, configurations and Guardian-series ordering. Two gauntlets plus two golem Void Cores cover the four recipes.
- All 56 server GameTests and binary/source distribution checks pass. Client visuals have not been manually verified.

## 0.1.0-dev.22

- Remove the 0.2-block visual height offset from Wither Smoke. Keep the server particle transport confirmed visible by the player in dev.21.
- Wall impacts were also invisible before dev.21, so impact-plane occlusion was not the demonstrated cause.

## 0.1.0-dev.21

- Send managed Wither Smoke visuals through vanilla server particle packets, covering both Howitzer impacts and the standalone glyph. Lift particle origins slightly above the impact plane; keep native cloud damage, shrinking and expiry.
- Suppress the managed cloud's local particle emitter to avoid duplicate smoke. Ordinary Cataclysm clouds retain their original behavior. Particle settings remain respected.
- The dev.19 tick-culling guard did not resolve the player's visual report; it is not a confirmed root-cause fix.

## 0.1.0-dev.10

- Craft Monstrous Quake from Infernal Forge plus Source Gem (27 experience), giving the native hammer a glyph use. Keep Boss damage behavior unchanged.
- Use the native hammer icon, including its builtin/entity renderer with the native item identity. Share the existing item-render delegation with Ceraunus glyphs.

## 0.1.0-dev.9

- Keep Monstrous Horn only for Earthbreaker and Flame Rings in the Monstrosity series. Craft Quake, Shoulder Check, Bone Fracture and Monstrous from Lava Power Cell instead; all eight remaining series glyphs now use cells.
- Preserve all glyph icons, one Source Gem and 27 experience per Scribe recipe.

## 0.1.0-dev.8

- Require Lava Power Cell plus Source Gem for Flare Bomb, replacing the craftable Monstrous Eye with a direct Boss drop. Retain the existing eye icon and 27 experience cost.

## 0.1.0-dev.7

- Add Bone Fracture as a native enemy status effect (40 Mana, 5 seconds), with amplification and duration modifiers.
- Add temporary Lava Walking for self/allies (80 Mana, 30 seconds), synchronized fluid collision, buoyancy, crouch-to-dive and scoped lava/burning protection. All behavior ends with the status.
- Add configurable durations, bilingual descriptions and real Scribe recipes using Monstrous Horn / Lava Power Cell plus Source Gem. Runtime-reference icons only.
- Fix shared carrier duration descriptions incorrectly mentioning Wave for all projectile replacements.
- All 26 server GameTests and binary/source resource-boundary checks pass; client visuals remain unverified.

## 0.1.0-dev.6

- 接入巨兽的熔岩炸弹、巨兽震地、撼地裂波、环形焰柱、巨兽肩撞、熔岩汲取，以及恶兽头盔的骇人之恶；共 15 个魔符。
- 相似攻击采用 Boss 实现，保留生命比例伤害、逐段冲击、骨裂及半血变化；通过独立调用帧复用本体，不复制其代码。
- Boss 独有能力使用恶兽犄角／熔岩能量单元制作，骇人之恶使用恶兽头盔；均为抄写台真实配方。
- 临时熔岩有独立原生生命周期和存档清理记录，禁止扩散；普通本体熔岩与投射物不受影响。重复载体按命中付费，空蓝停止。
- 新增冷却与参数配置、完整中英文本，复用本体图标、粒子、声音和地块外观。
- 23 项服务端 GameTest、文本检查、二进制及源码包资源边界检查通过。客户端外观未人工验证。

## 0.1.0-dev.5

- Fix Ars augment tooltips by supplying actual translated Components, reusing each projectile form's existing description across carriers.
- Delegate Ceraunus and Wave glyph icons to the native Ceraunus item renderer with a native item stack; builtin/entity model inheritance alone cannot render them.
- Add Netherite Monstrosity Flare Bomb: native flight, trail, impact event, cross/diagonal pattern and delayed Flame Jets.
- Apply Harm amplification to both the direct hit and generated jets; retain native, unmanaged Boss projectiles unchanged.
- Default to 300 trigger Mana and 8 seconds of active cooldown; include all secondary jets in the launch/impact payment.
- Add a real Scribe recipe from Monstrous Eye plus Source Gem and runtime-only icon reference.
- Extend ordinary/repeating projectile tests and add tooltip-key and full-pattern lifecycle regressions.

## 0.1.0-dev.4

- Add Ceraunus and Wave projectile replacements using native registered Cataclysm entities.
- Keep anchor retrieval after a single impact; repeated carriers keep their trajectories and pay per hit.
- Keep native wave contact damage, Wetness, knockback, movement and animation; continue the Ars phrase once per target.
- Set Wave to 9 damage, 4 seconds and 90 Mana; Extend Time adds 50% of the baseline duration per modifier.
- Add configurable active cooldowns through Conflux: Ceraunus 2 seconds, Wave 5 seconds, creative bypass by default.
- Add Scribe recipes, runtime-only icon paths, bilingual text, duration validation and native lifecycle tests.

## 0.1.0-dev.3

- Add Void Rune and Flame Jet position-effect glyphs using native registered entities.
- Retain native animation events, particles, sounds, damage windows, alliance checks and ignition.
- Share effect context and trigger-time payment with Bolt Strike; no Conflux changes.
- Project ground effects onto nearby collision surfaces, including slabs; failed placement does not charge.
- Add Scribe recipes, runtime-only native icon references, Chinese/English text and balance configuration.
- Add integration coverage for delayed damage, fire, allies, cleanup, insufficient mana, creative and owner separation.

## 0.1.0-dev.2

- Fix missing Ars glyph-name localization keys in Chinese and English.
- Validate registered ability name and description translations before packaging.

## 0.1.0-dev.1

- Start the independent NeoForge 1.21.1 addon, requiring the original Cataclysm.
- Add Amethyst Cluster and Lionfish Spike projectile replacements and Bolt Strike effect.
- Call native registered entities and their impact handlers; preserve original native projectiles.
- Reuse Ars Conflux carrier profiles, trajectory, context, billing and display ordering.
- Add native Scribe recipes, English/Chinese text and server balance configuration.
- Reference installed Cataclysm item-model paths without copying upstream resources.
- Check both binary and source artifacts for foreign assets, classes and embedded dependencies.
- Add four passing server integration tests. Client visual checks remain manual.
