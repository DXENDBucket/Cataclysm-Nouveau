package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Ignited_Berserker_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.SpellBerserker;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=Ignited_Berserker_Entity.class,remap=false)
public abstract class BerserkerAdapterMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/Internal_Animation_Monster;aiStep()V"))
    private void berserker$frameOnly(Ignited_Berserker_Entity e,Operation<Void> original){if(!((Object)this instanceof SpellBerserker))original.call(e);}
    @ModifyArg(method={"aiStep","AreaAttack"},at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=0)
    private LivingEntity berserker$owner(LivingEntity owner){return (Object)this instanceof SpellBerserker f?(f.cast.damageOwner()==null?f.cast.caster():f.cast.damageOwner()):owner;}
}
