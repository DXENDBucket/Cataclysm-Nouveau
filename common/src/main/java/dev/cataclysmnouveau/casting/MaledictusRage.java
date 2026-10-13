package dev.cataclysmnouveau.casting;

import dev.cataclysmnouveau.CataclysmConfig;
import dev.cataclysmnouveau.CataclysmEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/** Shared state for active glyphs and native successful-hit/consume adapters. */
public final class MaledictusRage {
    public static final int MAX_STACKS=5;
    public static int stacks(LivingEntity actor) {
        var effect=actor==null?null:actor.getEffect(CataclysmEffects.CURSED_RAGE);
        return effect==null?0:Math.min(MAX_STACKS,effect.getAmplifier()+1);
    }
    /** Gain refreshes the first decay interval, including at the cap. Server authority only. */
    public static boolean gain(LivingEntity actor,int amount) {
        if(actor==null || actor.level().isClientSide || !actor.isAlive() || amount<=0)return false;
        return set(actor,(int)Math.min(MAX_STACKS,(long)stacks(actor)+amount));
    }
    public static int consume(LivingEntity actor,int amount) {
        if(actor==null || actor.level().isClientSide || amount<=0)return 0;
        int taken=Math.min(stacks(actor),amount);
        if(taken>0)set(actor,stacks(actor)-taken);
        return taken;
    }
    public static void clear(LivingEntity actor) {
        if(actor!=null && !actor.level().isClientSide)actor.removeEffect(CataclysmEffects.CURSED_RAGE);
    }
    private static boolean set(LivingEntity actor,int stacks) {
        // Vanilla hidden-effect chains already tick, save and synchronize each level transition.
        // Lower levels have longer absolute durations because every hidden duration ticks too.
        MobEffectInstance chain=null;
        int interval=CataclysmConfig.rageDecay();
        for(int level=1;level<=stacks;level++)
            chain=new MobEffectInstance(CataclysmEffects.CURSED_RAGE,interval*(stacks-level+1),
                    level-1,false,true,true,chain);
        clear(actor);
        return chain==null || actor.addEffect(chain);
    }
    public static float arrowDamage(double base,LivingEntity caster) {return (float)(base*(1+.05*stacks(caster)));}
    public static float halberdDamage(double base,LivingEntity caster) {return (float)(base*(1+.10*stacks(caster)));}
    private MaledictusRage() {}
}
