package dev.cataclysmnouveau;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Forge bridge for the newer damage-calculator hooks, restricted to this addon's explosions. */
@Mod.EventBusSubscriber(modid="cataclysm_nouveau")
public class ForgeExplosionCalculator extends ExplosionDamageCalculator {
    private static final ThreadLocal<Float> BASE=new ThreadLocal<>();
    public boolean shouldDamageEntity(Explosion explosion,Entity target){return true;}
    public float getKnockbackMultiplier(Entity target){return 1;}
    public float getEntityDamageAmount(Explosion explosion,Entity target){return BASE.get();}
    public float adjust(Explosion explosion,Entity target,float amount){
        Float previous=BASE.get();BASE.set(amount);
        try{return getEntityDamageAmount(explosion,target);}
        finally{if(previous==null)BASE.remove();else BASE.set(previous);}
    }
    @SubscribeEvent public static void filter(ExplosionEvent.Detonate event){
        if(((dev.cataclysmnouveau.mixin.ForgeExplosionAccess)event.getExplosion()).confluxCataclysm$calculator() instanceof ForgeExplosionCalculator calculator)
            event.getAffectedEntities().removeIf(e->!calculator.shouldDamageEntity(event.getExplosion(),e));
    }
}
