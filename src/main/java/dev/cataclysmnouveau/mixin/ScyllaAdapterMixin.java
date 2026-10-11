package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=Scylla_Entity.class,remap=false)
public abstract class ScyllaAdapterMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/IABossMonsters/IABoss_monster;aiStep()V"))
    private void confluxScylla$noMobTick(Scylla_Entity boss,Operation<Void> original) {
        if(!((Object)this instanceof SpellScylla)) original.call(boss);
    }
    @ModifyArg(method={"aiStep","spawnLightning"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),index=0)
    private Entity confluxScylla$child(Entity child) {
        return (Object)this instanceof SpellScylla frame?ScyllaCasting.bossChild(child,frame):child;
    }
    @ModifyArg(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/util/CMDamageTypes;causeLightningMobDamage(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;"),index=0)
    private LivingEntity confluxScylla$owner(LivingEntity original) {
        return (Object)this instanceof SpellScylla frame?frame.cast.damageOwner():original;
    }
    @ModifyArg(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),index=1)
    private float confluxScylla$damage(float original) {
        return (Object)this instanceof SpellScylla frame?Math.max(0,original+frame.bonus):original;
    }
}
