# Native adapter boundary

This addon supplies glyph declarations, native payload selection, a small amount of
balance data, and opt-in hooks on two Cataclysm projectile classes. Conflux remains
the shared source of carrier compatibility, owner/executor distinction, deferred
continuations, execution cleanup and resource transactions. No Cataclysm-specific
rule was added to Conflux.

## Projectile state

- Mode 0: ordinary Cataclysm entity, retaining original movement, damage and saves.
- Mode 1: native visual driven by an off-world Ars carrier. Native movement is
  suppressed; native `onHit` is invoked at the Ars impact. The native discard is
  suppressed only during a repeated paid hit, so orbit can hit again. Cleanup
  disables that guard before discarding the visual.
- Mode 2: native flight with its own gravity. An opt-in impact callback wraps native
  `onHit`, then continues the Ars phrase. A recursion guard prevents duplicate hits.

Only the mode is synchronized. Resolver references, callbacks and damage bonuses
are transient server state. Managed entities are not saved because their execution
sessions cannot be reconstructed after a world reload.

The lionfish hook modifies only the argument to native damage calls, only on managed
entities. Its original damage handler, poison, alliance checks and client particles
remain in Cataclysm. The amethyst entity already exposes its damage setter. Neither
native method body is copied into the addon.

## Ownership and continuations

Native payload ownership follows `CastContext.damageOwner`. Resource payment uses
the wrapped Ars caster. Selecting another entity never transfers payment or damage
credit to it. Continuations receive the current scoped hit context and the inherited
resource account rather than imposing this addon's account on the next provider.
The continuation uses the payload's current world, including Conflux trajectory
relocation.

## Visual resources

Ars glyph names use `cataclysm_nouveau.glyph_name.<registry path>` in the language
files, including both spellbook labels and glyph item names. Ordinary `item.*`
translations alone do not satisfy that contract. `verifyGlyphLocalization` checks
the names and descriptions in English and Chinese before packaging.

Only three independently authored item-model JSON files exist. Each has a single
`parent` reference into the installed Cataclysm namespace. Native entity renderers,
entity events, particles and sounds are supplied by the original mod. This project
does not unpack their assets or patch the original JAR.

Upstream inspected: release 3.33 and branch `1.21`, commit
`10e21131b8eb2ce92be416409f91925e8c5decf0`. Class signatures were checked against the
release JAR. Dependency inputs are ignored locally and excluded from both artifacts.
