package dev.cataclysmnouveau.casting;

import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.*;
import com.hollingsworth.arsnouveau.common.spell.validation.SpellPhraseValidator;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.glyph.CataclysmProjectileGlyph;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.*;

/** Carrier compatibility is widened at registration; actual payload rules remain addon-owned. */
public final class PayloadValidation {
    public static List<SpellValidationError> validate(List<AbstractSpellPart> recipe) {
        var errors=new ArrayList<SpellValidationError>();
        for(var phrase:SpellPhraseValidator.splitSpellIntoPhrases(recipe)) {
            var profile=CarrierRegistry.of(phrase.getAction());
            if(profile==null) continue;
            var forms=phrase.getAugments().stream().filter(ProjectileForm.class::isInstance).toList();
            if(!forms.isEmpty() && !(forms.getFirst() instanceof CataclysmProjectileGlyph)) continue;
            var own=forms.isEmpty()?null:(CataclysmProjectileGlyph)forms.getFirst();
            boolean invalid=phrase.getAugments().stream().anyMatch(augment->{
                if(augment instanceof ProjectileForm) return false;
                if(own==null) return !profile.originallySupported(augment) &&
                        (augment==AugmentExtendTime.INSTANCE || augment==AugmentDurationDown.INSTANCE || augment==AugmentAmplify.INSTANCE || augment==AugmentDampen.INSTANCE);
                if(own.ability==Ability.WAVE && (augment==AugmentExtendTime.INSTANCE || augment==AugmentDurationDown.INSTANCE)) return false;
                return !profile.augments().contains(augment);
            });
            if(invalid) errors.add(new Error(phrase.getFirstPosition(),phrase.getAction()));
        }
        return errors;
    }
    private record Error(int position,AbstractSpellPart spellPart) implements SpellValidationError {
        public int getPosition() {return position;}
        public AbstractSpellPart getSpellPart() {return spellPart;}
        public MutableComponent makeTextComponentExisting() {return Component.translatable("cataclysm_nouveau.validation.modifier");}
        public MutableComponent makeTextComponentAdding() {return makeTextComponentExisting();}
    }
    private PayloadValidation() {}
}
