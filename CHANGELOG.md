# Changelog

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
