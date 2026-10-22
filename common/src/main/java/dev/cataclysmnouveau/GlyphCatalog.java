package dev.cataclysmnouveau;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import dev.arsconflux.api.glyph.GlyphPresentation;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.cataclysmnouveau.glyph.*;
import java.util.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
public final class GlyphCatalog {
    public static final CataclysmProjectileGlyph AXE_BLADE=new CataclysmProjectileGlyph(Ability.AXE_BLADE);
    public static final CataclysmEffectGlyph APTRGANGR_SMASH=new CataclysmEffectGlyph(Ability.APTRGANGR_SMASH);
    public static final CataclysmEffectGlyph ASHEN_BREATH=new CataclysmEffectGlyph(Ability.ASHEN_BREATH);
    public static final CataclysmProjectileGlyph BLAZING_BONE=new CataclysmProjectileGlyph(Ability.BLAZING_BONE);
    public static final CataclysmEffectGlyph BONE_STORM=new CataclysmEffectGlyph(Ability.BONE_STORM);
    public static final CataclysmProjectileGlyph AMETHYST=new CataclysmProjectileGlyph(Ability.AMETHYST_CLUSTER);
    public static final CataclysmProjectileGlyph SPIKE=new CataclysmProjectileGlyph(Ability.LIONFISH_SPIKE);
    public static final CataclysmEffectGlyph BOLT=new CataclysmEffectGlyph(Ability.BOLT_STRIKE);
    public static final CataclysmEffectGlyph RUNE=new CataclysmEffectGlyph(Ability.VOID_RUNE);
    public static final CataclysmEffectGlyph JET=new CataclysmEffectGlyph(Ability.FLAME_JET);
    public static final CataclysmProjectileGlyph ANCHOR=new CataclysmProjectileGlyph(Ability.CERAUNUS);
    public static final CataclysmProjectileGlyph WAVE=new CataclysmProjectileGlyph(Ability.WAVE);
    public static final CataclysmProjectileGlyph FLARE=new CataclysmProjectileGlyph(Ability.FLARE_BOMB);
    public static final CataclysmProjectileGlyph LAVA=new CataclysmProjectileGlyph(Ability.LAVA_BOMB);
    public static final CataclysmEffectGlyph QUAKE=new CataclysmEffectGlyph(Ability.EARTHQUAKE);
    public static final CataclysmEffectGlyph FISSURE=new CataclysmEffectGlyph(Ability.FISSURE);
    public static final CataclysmEffectGlyph RINGS=new CataclysmEffectGlyph(Ability.FLAME_RING);
    public static final CataclysmEffectGlyph CHARGE=new CataclysmEffectGlyph(Ability.SHOULDER_CHECK);
    public static final CataclysmEffectGlyph ABSORB=new CataclysmEffectGlyph(Ability.LAVA_ABSORPTION);
    public static final CataclysmEffectGlyph MONSTROUS=new CataclysmEffectGlyph(Ability.MONSTROUS);
    public static final CataclysmEffectGlyph FRACTURE=new CataclysmEffectGlyph(Ability.BONE_FRACTURE);
    public static final CataclysmEffectGlyph LAVA_WALKING=new CataclysmEffectGlyph(Ability.LAVA_WALKING);
    public static final CataclysmProjectileGlyph WATER_SPEAR=new CataclysmProjectileGlyph(Ability.WATER_SPEAR);
    public static final CataclysmProjectileGlyph LIGHTNING_SPEAR=new CataclysmProjectileGlyph(Ability.LIGHTNING_SPEAR);
    public static final CataclysmEffectGlyph SERPENT=new CataclysmEffectGlyph(Ability.STORM_SERPENT);
    public static final CataclysmEffectGlyph STORM=new CataclysmEffectGlyph(Ability.LIGHTNING_STORM);
    public static final CataclysmEffectGlyph FIELD=new CataclysmEffectGlyph(Ability.LIGHTNING_FIELD);
    public static final CataclysmEffectGlyph BURST=new CataclysmEffectGlyph(Ability.STORM_BURST);
    public static final CataclysmProjectileGlyph SPARK=new CataclysmProjectileGlyph(Ability.SPARK);
    public static final CataclysmEffectGlyph CLOUD=new CataclysmEffectGlyph(Ability.THUNDER_CLOUD);
    public static final CataclysmEffectGlyph ROW=new CataclysmEffectGlyph(Ability.LIGHTNING_ROW);
    public static final CataclysmEffectGlyph COMBO=new CataclysmEffectGlyph(Ability.THUNDER_COMBO);
    public static final CataclysmEffectGlyph SPIN=new CataclysmEffectGlyph(Ability.ANCHOR_SPIN);
    public static final CataclysmEffectGlyph LEAP=new CataclysmEffectGlyph(Ability.ANCHOR_LEAP);
    public static final CataclysmEffectGlyph WATER_WALKING=new CataclysmEffectGlyph(Ability.WATER_WALKING);
    public static final CataclysmProjectileGlyph LASER_BOLT=new CataclysmProjectileGlyph(Ability.LASER_BOLT);
    public static final CataclysmProjectileGlyph MISSILE=new CataclysmProjectileGlyph(Ability.WITHER_MISSILE);
    public static final CataclysmProjectileGlyph HOMING=new CataclysmProjectileGlyph(Ability.HOMING_MISSILE);
    public static final CataclysmProjectileGlyph HOWITZER=new CataclysmProjectileGlyph(Ability.WITHER_HOWITZER);
    public static final CataclysmEffectGlyph DEATH_LASER=new CataclysmEffectGlyph(Ability.DEATH_LASER);
    public static final CataclysmEffectGlyph HARBINGER_CHARGE=new CataclysmEffectGlyph(Ability.HARBINGER_CHARGE);
    public static final CataclysmEffectGlyph HOWITZER_VOLLEY=new CataclysmEffectGlyph(Ability.HOWITZER_VOLLEY);
    public static final CataclysmEffectGlyph MISSILE_SALVO=new CataclysmEffectGlyph(Ability.MISSILE_SALVO);
    public static final CataclysmEffectGlyph SMOKE=new CataclysmEffectGlyph(Ability.WITHER_SMOKE);
    public static final CataclysmProjectileGlyph GUARDIAN_BULLET=new CataclysmProjectileGlyph(Ability.GUARDIAN_BULLET);
    public static final CataclysmEffectGlyph VORTEX=new CataclysmEffectGlyph(Ability.VOID_VORTEX);
    public static final CataclysmEffectGlyph GUARDIAN_STOMP=new CataclysmEffectGlyph(Ability.GUARDIAN_STOMP);
    public static final CataclysmEffectGlyph GRAVITY_PUNCH=new CataclysmEffectGlyph(Ability.GRAVITY_PUNCH);
    public static final CataclysmEffectGlyph GUARDIAN_UPPERCUT=new CataclysmEffectGlyph(Ability.GUARDIAN_UPPERCUT);
    public static final CataclysmEffectGlyph GUARDIAN_BLINK=new CataclysmEffectGlyph(Ability.GUARDIAN_BLINK);
    public static final CataclysmEffectGlyph GUARDIAN_AIR_STRIKE=new CataclysmEffectGlyph(Ability.GUARDIAN_AIR_STRIKE);
    public static final CataclysmEffectGlyph GUARDIAN_ROCKET_PUNCH=new CataclysmEffectGlyph(Ability.GUARDIAN_ROCKET_PUNCH);
    public static final CataclysmProjectileGlyph IGNIS_FIREBALL=new CataclysmProjectileGlyph(Ability.IGNIS_FIREBALL);
    public static final CataclysmProjectileGlyph IGNIS_ABYSS_FIREBALL=new CataclysmProjectileGlyph(Ability.IGNIS_ABYSS_FIREBALL);
    public static final CataclysmEffectGlyph IGNIS_BLADE_FIREBALLS=new CataclysmEffectGlyph(Ability.IGNIS_BLADE_FIREBALLS);
    public static final CataclysmEffectGlyph IGNIS_AIR_SMASH=new CataclysmEffectGlyph(Ability.IGNIS_AIR_SMASH);
    public static final CataclysmEffectGlyph IGNIS_SWORD_COMBO=new CataclysmEffectGlyph(Ability.IGNIS_SWORD_COMBO);
    public static final CataclysmEffectGlyph IGNIS_FIREBALL_ARRAY=new CataclysmEffectGlyph(Ability.IGNIS_FIREBALL_ARRAY);
    public static final CataclysmEffectGlyph IGNIS_EARTH_SHUDDERS=new CataclysmEffectGlyph(Ability.IGNIS_EARTH_SHUDDERS);
    public static final CataclysmEffectGlyph IGNIS_REINFORCED_SMASH=new CataclysmEffectGlyph(Ability.IGNIS_REINFORCED_SMASH);
    public static final CataclysmEffectGlyph IGNIS_ULTIMATE=new CataclysmEffectGlyph(Ability.IGNIS_ULTIMATE);
    public static final CataclysmEffectGlyph IGNIS_FLAME_STRIKE=new CataclysmEffectGlyph(Ability.IGNIS_FLAME_STRIKE);
    public static final CataclysmProjectileGlyph PHANTOM_ARROW=new CataclysmProjectileGlyph(Ability.PHANTOM_ARROW);
    public static final CataclysmEffectGlyph PHANTOM_HALBERD=new CataclysmEffectGlyph(Ability.PHANTOM_HALBERD);
    public static final CataclysmEffectGlyph RAGE=new CataclysmEffectGlyph(Ability.CURSED_RAGE);
    public static final CataclysmEffectGlyph PHANTOM_SPIRAL=new CataclysmEffectGlyph(Ability.PHANTOM_SPIRAL);
    public static final CataclysmEffectGlyph PHANTOM_RINGS=new CataclysmEffectGlyph(Ability.PHANTOM_RINGS);
    public static final CataclysmEffectGlyph HALBERD_CLEAVE=new CataclysmEffectGlyph(Ability.HALBERD_CLEAVE);
    public static final CataclysmEffectGlyph SOUL_BURST=new CataclysmEffectGlyph(Ability.SOUL_BURST);
    public static final CataclysmEffectGlyph MALEDICTUS_AERIAL_SMASH=new CataclysmEffectGlyph(Ability.MALEDICTUS_AERIAL_SMASH);
    public static final CataclysmEffectGlyph PHANTOM_AERIAL_VOLLEY=new CataclysmEffectGlyph(Ability.PHANTOM_AERIAL_VOLLEY);
    public static final CataclysmEffectGlyph MALEDICTUS_RUSH=new CataclysmEffectGlyph(Ability.MALEDICTUS_RUSH);
    public static final CataclysmEffectGlyph MALEDICTUS_SHOCKWAVE=new CataclysmEffectGlyph(Ability.MALEDICTUS_SHOCKWAVE);
    public static final CataclysmEffectGlyph MALEDICTUS_UPPERCUT=new CataclysmEffectGlyph(Ability.MALEDICTUS_UPPERCUT);
    public static final CataclysmEffectGlyph MALEDICTUS_MELEE_COMBO=new CataclysmEffectGlyph(Ability.MALEDICTUS_MELEE_COMBO);
    public static final CataclysmEffectGlyph MALEDICTUS_SPIN_SLASHES=new CataclysmEffectGlyph(Ability.MALEDICTUS_SPIN_SLASHES);
    public static final CataclysmProjectileGlyph ABYSS_ORB=new CataclysmProjectileGlyph(Ability.ABYSS_ORB);
    public static final CataclysmEffectGlyph ABYSS_MINE=new CataclysmEffectGlyph(Ability.ABYSS_MINE);
    public static final CataclysmEffectGlyph ABYSS_BLAST=new CataclysmEffectGlyph(Ability.ABYSS_BLAST);
    public static final CataclysmEffectGlyph ABYSS_BOMBARDMENT=new CataclysmEffectGlyph(Ability.ABYSS_BOMBARDMENT);
    public static final CataclysmEffectGlyph DIMENSIONAL_RIFT=new CataclysmEffectGlyph(Ability.DIMENSIONAL_RIFT);
    public static final CataclysmEffectGlyph TIDAL_TENTACLE=new CataclysmEffectGlyph(Ability.TIDAL_TENTACLE);
    public static final CataclysmEffectGlyph ABYSS_MINEFIELD=new CataclysmEffectGlyph(Ability.ABYSS_MINEFIELD);
    public static final CataclysmEffectGlyph ABYSS_BOMBARDMENT_ARRAY=new CataclysmEffectGlyph(Ability.ABYSS_BOMBARDMENT_ARRAY);
    public static final CataclysmEffectGlyph ABYSS_ORB_ARRAY=new CataclysmEffectGlyph(Ability.ABYSS_ORB_ARRAY);
    public static final CataclysmEffectGlyph ABYSS_TRIPLE_BLAST=new CataclysmEffectGlyph(Ability.ABYSS_TRIPLE_BLAST);
    public static final CataclysmEffectGlyph SANDSTORM_RING=new CataclysmEffectGlyph(Ability.SANDSTORM_RING);
    public static final CataclysmEffectGlyph DESERT_STELE=new CataclysmEffectGlyph(Ability.DESERT_STELE);
    public static final CataclysmProjectileGlyph GROUND_QUAKE=new CataclysmProjectileGlyph(Ability.GROUND_QUAKE);
    public static final CataclysmEffectGlyph REMNANT_CHARGE=new CataclysmEffectGlyph(Ability.REMNANT_CHARGE);
    public static final CataclysmEffectGlyph REMNANT_TAIL_SWEEP=new CataclysmEffectGlyph(Ability.REMNANT_TAIL_SWEEP);
    public static final CataclysmEffectGlyph REMNANT_STOMP=new CataclysmEffectGlyph(Ability.REMNANT_STOMP);
    public static final CataclysmEffectGlyph DESERT_STELE_ARRAY=new CataclysmEffectGlyph(Ability.DESERT_STELE_ARRAY);
    public static final CataclysmEffectGlyph REMNANT_TRIPLE_QUAKE=new CataclysmEffectGlyph(Ability.REMNANT_TRIPLE_QUAKE);
    public static final CataclysmProjectileGlyph CURSED_SANDSTORM=new CataclysmProjectileGlyph(Ability.CURSED_SANDSTORM);
    public static final CataclysmEffectGlyph BERSERKER_CROSS_SLASH=new CataclysmEffectGlyph(Ability.BERSERKER_CROSS_SLASH);
    public static final CataclysmEffectGlyph BERSERKER_SWORD_DANCE=new CataclysmEffectGlyph(Ability.BERSERKER_SWORD_DANCE);
    public static final CataclysmEffectGlyph BERSERKER_SPIN=new CataclysmEffectGlyph(Ability.BERSERKER_SPIN);
    public static final CataclysmEffectGlyph ABYSS_MARK=new CataclysmEffectGlyph(Ability.ABYSS_MARK);
    public static final CataclysmEffectGlyph PRIEST_GLARE=new CataclysmEffectGlyph(Ability.PRIEST_GLARE);
    public static final CataclysmProjectileGlyph VOID_HOWITZER=new CataclysmProjectileGlyph(Ability.VOID_HOWITZER);
    public static final CataclysmProjectileGlyph VOID_SCATTER_ARROW=new CataclysmProjectileGlyph(Ability.VOID_SCATTER_ARROW);
    public static final List<CataclysmProjectileGlyph> PROJECTILES=List.of(AMETHYST,SPIKE,ANCHOR,WAVE,FLARE,LAVA,WATER_SPEAR,LIGHTNING_SPEAR,SPARK,LASER_BOLT,MISSILE,HOMING,HOWITZER,GUARDIAN_BULLET,IGNIS_FIREBALL,IGNIS_ABYSS_FIREBALL,PHANTOM_ARROW,ABYSS_ORB,GROUND_QUAKE,CURSED_SANDSTORM,BLAZING_BONE,AXE_BLADE,VOID_HOWITZER,VOID_SCATTER_ARROW);
    public static final List<AbstractSpellPart> ALL=List.<AbstractSpellPart>of(BOLT,RUNE,JET,QUAKE,FISSURE,RINGS,CHARGE,ABSORB,MONSTROUS,FRACTURE,LAVA_WALKING,SERPENT,STORM,FIELD,BURST,AMETHYST,SPIKE,ANCHOR,WAVE,FLARE,LAVA,WATER_SPEAR,LIGHTNING_SPEAR,SPARK,CLOUD,ROW,COMBO,SPIN,LEAP,WATER_WALKING,LASER_BOLT,MISSILE,HOMING,HOWITZER,DEATH_LASER,HARBINGER_CHARGE,HOWITZER_VOLLEY,MISSILE_SALVO,SMOKE,GUARDIAN_BULLET,VORTEX,GUARDIAN_STOMP,GRAVITY_PUNCH,GUARDIAN_UPPERCUT,GUARDIAN_BLINK,GUARDIAN_AIR_STRIKE,GUARDIAN_ROCKET_PUNCH,IGNIS_FIREBALL,IGNIS_ABYSS_FIREBALL,IGNIS_BLADE_FIREBALLS,IGNIS_AIR_SMASH,IGNIS_SWORD_COMBO,IGNIS_FIREBALL_ARRAY,IGNIS_EARTH_SHUDDERS,IGNIS_REINFORCED_SMASH,IGNIS_ULTIMATE,IGNIS_FLAME_STRIKE,PHANTOM_ARROW,PHANTOM_HALBERD,RAGE,PHANTOM_SPIRAL,PHANTOM_RINGS,HALBERD_CLEAVE,SOUL_BURST,MALEDICTUS_AERIAL_SMASH,PHANTOM_AERIAL_VOLLEY,MALEDICTUS_RUSH,MALEDICTUS_SHOCKWAVE,MALEDICTUS_UPPERCUT,MALEDICTUS_MELEE_COMBO,MALEDICTUS_SPIN_SLASHES,ABYSS_ORB,ABYSS_MINE,ABYSS_BLAST,ABYSS_BOMBARDMENT,DIMENSIONAL_RIFT,TIDAL_TENTACLE,ABYSS_MINEFIELD,ABYSS_BOMBARDMENT_ARRAY,ABYSS_ORB_ARRAY,ABYSS_TRIPLE_BLAST,SANDSTORM_RING,DESERT_STELE,GROUND_QUAKE,REMNANT_STOMP,REMNANT_CHARGE,REMNANT_TAIL_SWEEP,DESERT_STELE_ARRAY,REMNANT_TRIPLE_QUAKE,CURSED_SANDSTORM,ASHEN_BREATH,BLAZING_BONE,BONE_STORM,AXE_BLADE,APTRGANGR_SMASH,BERSERKER_CROSS_SLASH,BERSERKER_SWORD_DANCE,BERSERKER_SPIN,ABYSS_MARK,PRIEST_GLARE,VOID_HOWITZER,VOID_SCATTER_ARROW)
            .stream().sorted(Comparator.comparing(GlyphCatalog::displayOrderKey)).toList();
    /** Core retains Ars categories; within each category, group native series before registry names. */
    public static String displayOrderKey(AbstractSpellPart part) {
        Ability ability=part instanceof CataclysmProjectileGlyph projectile?projectile.ability
                :part instanceof CataclysmEffectGlyph effect?effect.ability:null;
        if(ability==null) return "2";
        return String.format(Locale.ROOT,"%d/%02d/%03d",part instanceof CataclysmProjectileGlyph?1:0,
                ability.series().ordinal(),ability.ordinal());
    }
    public static CataclysmProjectileGlyph projectile(Ability ability) { return PROJECTILES.stream().filter(g->g.ability==ability).findFirst().orElseThrow(); }
    public static void register() {
        GlyphPresentation.register("cataclysm_nouveau",GlyphCatalog::displayOrderKey);
        ALL.forEach(GlyphRegistry::registerSpell);
        dev.cataclysmnouveau.casting.CataclysmActiveCasts.register();
        dev.arsconflux.api.glyph.SpellValidators.register(CarrierRegistry.id("cataclysm_nouveau:payload_rules"),dev.cataclysmnouveau.casting.PayloadValidation::validate);
    }
    public static void configureCarriers() {
        for (var profile:CarrierRegistry.profiles()) if (profile.glyph()!=null) {
            profile.rememberOriginalAugments(profile.glyph());profile.glyph().compatibleAugments.addAll(PROJECTILES);
            // Ars reads this map directly: reuse the form's description for all carriers.
            for (var form : PROJECTILES) profile.glyph().augmentDescriptions.put(form,
                    net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.glyph_desc.glyph_"+form.ability.id));
            profile.glyph().compatibleAugments.addAll(profile.augments());
            profile.glyph().compatibleAugments.add(AugmentExtendTime.INSTANCE);
            profile.glyph().compatibleAugments.add(AugmentDurationDown.INSTANCE);
            profile.glyph().augmentDescriptions.putIfAbsent(AugmentExtendTime.INSTANCE,
                    net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.augment.duration_up"));
            profile.glyph().augmentDescriptions.putIfAbsent(AugmentDurationDown.INSTANCE,
                    net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.augment.duration_down"));
        }
    }
    private GlyphCatalog() {}
}
