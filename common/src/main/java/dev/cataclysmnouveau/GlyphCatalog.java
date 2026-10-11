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
    public static final List<CataclysmProjectileGlyph> PROJECTILES=List.of(AMETHYST,SPIKE,ANCHOR,WAVE,FLARE);
    public static final List<AbstractSpellPart> ALL=List.of(BOLT,RUNE,JET,AMETHYST,SPIKE,ANCHOR,WAVE,FLARE);
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
                    net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.augment.wave_duration_up"));
            profile.glyph().augmentDescriptions.putIfAbsent(AugmentDurationDown.INSTANCE,
                    net.minecraft.network.chat.Component.translatable("cataclysm_nouveau.augment.wave_duration_down"));
        }
    }
    private GlyphCatalog() {}
}
