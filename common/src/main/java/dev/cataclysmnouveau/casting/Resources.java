package dev.cataclysmnouveau.casting;
import com.hollingsworth.arsnouveau.api.spell.*;
import com.hollingsworth.arsnouveau.common.spell.augment.AugmentAmplify;
import com.hollingsworth.arsnouveau.common.spell.effect.EffectHarm;
import dev.arsconflux.api.resource.*;
import dev.cataclysmnouveau.*;
import java.util.List;
public final class Resources {
    public static ResourceCharge quote(SpellContext context, Ability ability, List<AbstractAugment> augments) {
        long extra=augments.stream().filter(a->a==AugmentAmplify.INSTANCE).count()*CataclysmConfig.amplifyCost();
        return new ResourceCharge(new ArsManaAccount(context.getCaster()),(int)Math.min(Integer.MAX_VALUE,CataclysmConfig.cost(ability)+extra));
    }
    public static float damageBonus(double amplify) { return (float)(EffectHarm.INSTANCE.AMP_VALUE.get()*amplify); }
    private Resources() {}
}
