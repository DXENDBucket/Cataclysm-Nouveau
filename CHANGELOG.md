# Changelog

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
