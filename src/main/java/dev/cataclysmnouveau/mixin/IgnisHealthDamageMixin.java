package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.sugar.Local;
import dev.cataclysmnouveau.casting.HealthDamage;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Audited native parameters supply each segment's fixed damage, without replacing attack bodies. */
@Mixin(value=Ignis_Entity.class,remap=false)
public abstract class IgnisHealthDamageMixin {
    private float bounded(LivingEntity target,Operation<Float> original,float damage,float fraction) {
        var boss=(Ignis_Entity)(Object)this;
        return HealthDamage.healthInput(boss,original.call(target),boss.getAttributeValue(Attributes.ATTACK_DAMAGE)*damage,fraction);
    }
    @WrapOperation(method={"AreaAttack","BodyCheckAttack","ShieldSmashDamage"},at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F",remap=true))
    private float ignis$attackHealth(LivingEntity target,Operation<Float> original,
            @Local(argsOnly=true,ordinal=3) float damage,@Local(argsOnly=true,ordinal=4) float fraction) {
        return bounded(target,original,damage,fraction);
    }
    @WrapOperation(method="UltimateAttack",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F",remap=true))
    private float ignis$ultimateHealth(LivingEntity target,Operation<Float> original,
            @Local(argsOnly=true,ordinal=2) float damage,@Local(argsOnly=true,ordinal=3) float fraction) {
        return bounded(target,original,damage,fraction);
    }
    @WrapOperation(method="Phase_Transition",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F",remap=true))
    private float ignis$phaseHealth(LivingEntity target,Operation<Float> original,
            @Local(argsOnly=true,ordinal=0) float damage,@Local(argsOnly=true,ordinal=1) float fraction) {
        return bounded(target,original,damage,fraction);
    }
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F",remap=true))
    private float ignis$smashHealth(LivingEntity target,Operation<Float> original) {
        return bounded(target,original,1.5F,.15F);
    }
    @WrapOperation(method="Poke",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F",remap=true))
    private float ignis$pokeHealth(LivingEntity target,Operation<Float> original) {
        return bounded(target,original,1,.1F);
    }
    @WrapOperation(method="positionRider",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;getMaxHealth()F",remap=true))
    private float ignis$riderHealth(LivingEntity target,Operation<Float> original) {
        return HealthDamage.healthInput((Ignis_Entity)(Object)this,original.call(target),4,.02F);
    }
}
