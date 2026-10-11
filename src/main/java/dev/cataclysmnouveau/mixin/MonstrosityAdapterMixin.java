package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.IABoss_monster;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.SpellMonstrosity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Every hook is opt-in to detached spell frames; real Bosses retain their native behavior. */
@Mixin(value=Netherite_Monstrosity_Entity.class,remap=false)
public abstract class MonstrosityAdapterMixin {
    @ModifyArg(method={"EarthQuake","spawnBlocks","aiStep"},at=@At(value="INVOKE",
            target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;"),index=0)
    private LivingEntity confluxCataclysm$owner(LivingEntity original) {
        return (Object)this instanceof SpellMonstrosity frame?frame.cast.damageOwner():original;
    }
    @ModifyArg(method={"EarthQuake","spawnBlocks","aiStep"},at=@At(value="INVOKE",
            target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),index=1)
    private float confluxCataclysm$damage(float original) {
        return (Object)this instanceof SpellMonstrosity frame?Math.max(0,original+frame.damageBonus):original;
    }
    @ModifyArg(method="spawnJet",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/projectile/Flame_Jet_Entity;<init>(Lnet/minecraft/world/level/Level;DDDFIFLnet/minecraft/world/entity/LivingEntity;)V"),index=7)
    private LivingEntity confluxCataclysm$jetOwner(LivingEntity original) {
        return (Object)this instanceof SpellMonstrosity frame?frame.cast.damageOwner():original;
    }
    @ModifyArg(method="spawnJet",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/projectile/Flame_Jet_Entity;<init>(Lnet/minecraft/world/level/Level;DDDFIFLnet/minecraft/world/entity/LivingEntity;)V"),index=6)
    private float confluxCataclysm$jetDamage(float original) {
        return (Object)this instanceof SpellMonstrosity frame?Math.max(0,original+frame.damageBonus):original;
    }
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/IABossMonsters/IABoss_monster;aiStep()V"))
    private void confluxCataclysm$noMobTick(Netherite_Monstrosity_Entity boss,Operation<Void> original) {
        if(!((Object)this instanceof SpellMonstrosity)) original.call(boss);
    }
    @Inject(method="ChargeBlockBreaking",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$noChargeGrief(CallbackInfo ci) {
        if((Object)this instanceof SpellMonstrosity) ci.cancel();
    }
    @Inject(method="doAbsorptionEffect",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$absorptionBoundary(BlockPos pos,CallbackInfo ci) {
        if(!((Object)this instanceof SpellMonstrosity frame)) return;
        if(!frame.level().hasChunkAt(pos) || !frame.level().getBlockState(pos).is(Blocks.LAVA)) {ci.cancel();return;}
        // Permission events are only needed for actual lava, not thousands of air positions.
        if(!dev.cataclysmnouveau.casting.TemporaryLava.mayChange(frame.cast.caster(),pos)) {ci.cancel();return;}
        frame.absorbed++;
    }
}
