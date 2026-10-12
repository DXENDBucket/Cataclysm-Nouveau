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
    public static float healingBonus(double amplify) { return (float)(EffectHarm.INSTANCE.AMP_VALUE.get()*amplify); }
    public static float augmentedDamage(double base,double amplify) {
        if(!Double.isFinite(base) || !Double.isFinite(amplify))return 0;
        double lower=CataclysmConfig.damageAugmentMin(),upper=Math.max(lower,CataclysmConfig.damageAugmentMax());
        double step=Math.min(upper,Math.max(lower,Math.max(0,base)*CataclysmConfig.damageAugmentFraction()));
        return (float)Math.max(0,base+amplify*step);
    }
    /** Counts, retained separately from base damage so child attacks scale from their own base. */
    public static float amplification(double value){return Double.isFinite(value)?(float)value:0;}
    private Resources() {}
}
