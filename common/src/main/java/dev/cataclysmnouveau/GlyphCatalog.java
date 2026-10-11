package dev.cataclysmnouveau;
import com.hollingsworth.arsnouveau.api.registry.GlyphRegistry;
import com.hollingsworth.arsnouveau.api.spell.AbstractSpellPart;
import dev.arsconflux.api.glyph.GlyphPresentation;
import dev.arsconflux.api.projectile.CarrierRegistry;
import dev.cataclysmnouveau.glyph.*;
import java.util.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
public final class GlyphCatalog {
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
    public static final List<CataclysmProjectileGlyph> PROJECTILES=List.of(AMETHYST,SPIKE,ANCHOR,WAVE,FLARE,LAVA,WATER_SPEAR,LIGHTNING_SPEAR);
    public static final List<AbstractSpellPart> ALL=List.of(BOLT,RUNE,JET,QUAKE,FISSURE,RINGS,CHARGE,ABSORB,MONSTROUS,FRACTURE,LAVA_WALKING,SERPENT,STORM,FIELD,BURST,AMETHYST,SPIKE,ANCHOR,WAVE,FLARE,LAVA,WATER_SPEAR,LIGHTNING_SPEAR);
    public static CataclysmProjectileGlyph projectile(Ability ability) { return PROJECTILES.stream().filter(g->g.ability==ability).findFirst().orElseThrow(); }
    public static void register() {
        GlyphPresentation.register("cataclysm_nouveau",g->g instanceof CataclysmProjectileGlyph?"1":"0");
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
