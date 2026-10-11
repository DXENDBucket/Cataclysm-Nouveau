package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Invoke the installed Boss's mechanics rather than copying its implementations. */
@Mixin(value=Netherite_Monstrosity_Entity.class,remap=false)
public interface MonstrosityAccess {
    @Invoker("EarthQuake") void confluxCataclysm$quake(double radius);
    @Invoker("StompDamage") void confluxCataclysm$stomp(float arc,int distance,int height,float vertical,float offset,float side,int shieldTicks,float factor);
    @Invoker("CircleFlameJet") void confluxCataclysm$rings(float offset,float side,int vertices,int rings,double delay);
    @Invoker("doAbsorptionEffects") void confluxCataclysm$absorb(int x,int y,int z);
}
