package dev.cataclysmnouveau;

import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import dev.arsconflux.api.glyph.GlyphAbilities;
import dev.arsconflux.api.glyph.GlyphConditions;
import dev.cataclysmnouveau.casting.MaledictusRage;
import net.minecraft.resources.ResourceLocation;
import java.util.Set;
import static dev.arsconflux.api.glyph.AbilityTags.*;

/** Hints describe the native receiver, not the affiliation or species of the caster. */
final class AbilityHints {
    static void register(AbstractSpellPart glyph, Ability ability) {
        GlyphAbilities.declare(glyph.getRegistryName(), tags(ability));
        switch (ability) {
            case MONSTROUS -> GlyphConditions.declare(glyph.getRegistryName(),
                    (caster, receiver) -> receiver != null && !receiver.hasEffect(ModEffect.EFFECTMONSTROUS));
            case LAVA_WALKING -> GlyphConditions.declare(glyph.getRegistryName(),
                    (caster, receiver) -> receiver != null && !receiver.hasEffect(CataclysmEffects.LAVA_WALKING));
            case WATER_WALKING -> GlyphConditions.declare(glyph.getRegistryName(),
                    (caster, receiver) -> receiver != null && !receiver.hasEffect(CataclysmEffects.WATER_WALKING));
            case CURSED_RAGE -> GlyphConditions.declare(glyph.getRegistryName(),
                    (caster, receiver) -> receiver != null && MaledictusRage.stacks(receiver) < MaledictusRage.MAX_STACKS);
            default -> { }
        }
    }

    /** Exhaustive so a newly added ability cannot silently inherit an arbitrary targeting policy. */
    private static Set<ResourceLocation> tags(Ability ability) {
        return switch (ability) {
            // A replacement is an attack, even when the next effect in the program is a buff.
            case AMETHYST_CLUSTER, LIONFISH_SPIKE, CERAUNUS, WATER_SPEAR,
                    LASER_BOLT, WITHER_MISSILE, HOMING_MISSILE, GUARDIAN_BULLET,
                    PHANTOM_ARROW, ABYSS_ORB, BLAZING_BONE, AXE_BLADE -> Set.of(ATTACK, ENEMY);
            case WAVE, FLARE_BOMB, LAVA_BOMB, LIGHTNING_SPEAR, SPARK, WITHER_HOWITZER,
                    IGNIS_FIREBALL, IGNIS_ABYSS_FIREBALL, GROUND_QUAKE, CURSED_SANDSTORM,
                    VOID_HOWITZER, VOID_SCATTER_ARROW -> Set.of(ATTACK, AREA, ENEMY);

            // These effects use the selected point; an enemy is a valid point, not an executor.
            case BOLT_STRIKE, VOID_RUNE, FLAME_JET, EARTHQUAKE, FISSURE, FLAME_RING,
                    STORM_SERPENT, LIGHTNING_STORM, LIGHTNING_FIELD, STORM_BURST,
                    THUNDER_CLOUD, LIGHTNING_ROW, WITHER_SMOKE, GUARDIAN_STOMP,
                    IGNIS_FLAME_STRIKE, PHANTOM_HALBERD, PHANTOM_SPIRAL, PHANTOM_RINGS,
                    SOUL_BURST, ABYSS_MINE, ABYSS_BOMBARDMENT, ABYSS_MINEFIELD,
                    ABYSS_BOMBARDMENT_ARRAY, DESERT_STELE, REMNANT_STOMP -> Set.of(ATTACK, AREA, SELF, ENEMY, POSITION);
            case VOID_VORTEX, DIMENSIONAL_RIFT -> Set.of(ATTACK, CONTROL, AREA, SELF, ENEMY, POSITION);
            case BONE_FRACTURE -> Set.of(CONTROL, ENEMY);
            case ABYSS_MARK, TIDAL_TENTACLE -> Set.of(ATTACK, ENEMY);

            // Beneficial anchoring means the receiver performs the attack on our behalf.
            case THUNDER_COMBO, ANCHOR_SPIN, HOWITZER_VOLLEY, MISSILE_SALVO,
                    GRAVITY_PUNCH, IGNIS_BLADE_FIREBALLS, IGNIS_SWORD_COMBO,
                    IGNIS_FIREBALL_ARRAY, IGNIS_EARTH_SHUDDERS, IGNIS_ULTIMATE,
                    HALBERD_CLEAVE, PHANTOM_AERIAL_VOLLEY, MALEDICTUS_SHOCKWAVE,
                    MALEDICTUS_MELEE_COMBO, MALEDICTUS_SPIN_SLASHES, ABYSS_ORB_ARRAY,
                    SANDSTORM_RING, DESERT_STELE_ARRAY, REMNANT_TRIPLE_QUAKE,
                    REMNANT_TAIL_SWEEP, BONE_STORM, APTRGANGR_SMASH,
                    BERSERKER_CROSS_SLASH, BERSERKER_SWORD_DANCE, BERSERKER_SPIN,
                    PRIEST_GLARE -> Set.of(ATTACK, AREA, SELF, ALLY, POSITION);
            case SHOULDER_CHECK, ANCHOR_LEAP, HARBINGER_CHARGE, GUARDIAN_UPPERCUT,
                    GUARDIAN_BLINK, GUARDIAN_AIR_STRIKE, GUARDIAN_ROCKET_PUNCH,
                    IGNIS_AIR_SMASH, IGNIS_REINFORCED_SMASH, MALEDICTUS_AERIAL_SMASH,
                    MALEDICTUS_RUSH, MALEDICTUS_UPPERCUT, REMNANT_CHARGE -> Set.of(ATTACK, MOBILITY, AREA, SELF, ALLY, POSITION);
            case DEATH_LASER, ABYSS_BLAST, ABYSS_TRIPLE_BLAST, ASHEN_BREATH -> Set.of(ATTACK, CHANNELING, AREA, SELF, ALLY, POSITION);

            // Absorption always heals the payer, even when its lava search is centered elsewhere.
            case LAVA_ABSORPTION -> Set.of(HEALING, SELF, POSITION);
            case MONSTROUS -> Set.of(DEFENSE, SELF, ALLY);
            case LAVA_WALKING -> Set.of(DEFENSE, MOBILITY, SELF, ALLY);
            case WATER_WALKING -> Set.of(MOBILITY, SELF, ALLY);
            case CURSED_RAGE -> Set.of(UTILITY, SELF, ALLY);
        };
    }
    private AbilityHints() { }
}
