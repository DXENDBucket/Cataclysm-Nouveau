# Native adapter boundary

This addon supplies glyph declarations, native payload selection, a small amount of
balance data, and opt-in hooks on selected Cataclysm payload classes. Conflux remains
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

The mode and wave acceleration multiplier are synchronized. Resolver references, callbacks and damage bonuses
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

Only independently authored item-model JSON files exist. Each has a single
`parent` reference into the installed Cataclysm namespace. Native entity renderers,
entity events, particles and sounds are supplied by the original mod. This project
does not unpack their assets or patch the original JAR.

Upstream inspected: release 3.33 and branch `1.21`, commit
`10e21131b8eb2ce92be416409f91925e8c5decf0`. Class signatures were checked against the
release JAR. Dependency inputs are ignored locally and excluded from both artifacts.

## Ground effects (dev.3)

`CataclysmEffectGlyph` supplies shared trigger payment and scoped damage ownership
for Bolt Strike, Void Rune and Flame Jet. Native entity constructors are called;
their tick methods and event handlers are neither replaced nor copied. No new
Mixin is needed. Subsequent Ars effects remain independent of the native delay.

Void Rune follows `CMCommonConfig.VoidCore.runeDamage`, Flame Jet follows
`CMCommonConfig.NetheriteMonstrosity.FlameJetDamage`, unless overridden in this
addon's server config. Amplify changes damage per native hit. Default warmup is
10 ticks; thereafter Cataclysm controls each attack window and removal. Flame Jet
retains its original five-second ignition only on successful non-allied hits.

Placement searches at most four blocks down from the selection and checks loaded
chunks before accessing collision shapes. This is a local ground projection, not
a request to load distant chunks or find the bottom of a cave. Failed placement
returns false to the existing Core transaction, so it does not charge. Actual
repeated triggers each spawn and pay independently; no second fee on damage ticks.

Client boundary checked in the 3.33 JAR: Void Rune uses its registered renderer,
synced activation/damage state and entity event 4; Flame Jet uses event 4 to begin
the native particle animation and sound. The native particle emits eight client
ticks after this event, matching its server damage timing. Client appearance still
requires manual verification; a server GameTest cannot establish visual correctness.

## Native anchor and wave (dev.4)

`NativeCarrierCasting` manages `ExecutionSession<Entity>` independently of the
older ThrowableProjectile driver. Ceraunus uses native AbstractArrow movement,
registered renderer, owner spawn data, chain and retrieval. Its first impact
continues once, switches to native return and retains the entity until retrieval
or the bounded session ends. Repeated Ars carriers prevent native retrieval and
permit only paid impact handlers. An opt-in entity event delegates block-contact
client particles directly to native `onHitBlock`; no particle body is copied.

Wave is not a Projectile. Its tick still owns native lifetime/state transitions,
particles, terrain movement, contact damage, extinguishing, Wetness and knockback.
Ars-driven carriers suppress only native movement and unpaid ambient attacks;
the real native contact handler is invoked for paid hits. Ordinary contact hooks
continue once per entity/block for each wave; later native damage is retained.
Launch acceleration is applied to the native forward acceleration vector.

Default wave lifetime is 80 ticks. One-shot duration modifiers scale it by
`1 + 0.5 * durationModifier`, bounded to 40–1200 ticks; recurring carriers already
include Ars duration modifiers in their expiration time. Validation exposes
Extend Time/Duration Down for wave payloads without enabling those modifiers for
unrelated plain Ars projectiles. Other providers retain their own modifier rules.

`CataclysmActiveCasts` uses the existing Core active-cast API and stores cooldown
expiry in caster data using the overworld clock. It gates new active uses only;
ongoing paid trajectories keep running. No new Core API, classes or assets were
modified. Native wave damage can hit more than once during its life, so its
resource price cannot be compared solely to a single nine-point hit.

Client appearance has not been manually exercised: chain endpoints, custom block
particle event, ground-wave fade and orbit animation require visual confirmation.
