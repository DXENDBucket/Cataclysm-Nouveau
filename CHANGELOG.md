# Changelog

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
