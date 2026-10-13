package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Maledictus_Entity.class,remap=false)
public abstract class MaledictusAdapterMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/InternalAnimationMonster/IABossMonsters/IABoss_monster;aiStep()V"))
    private void maledictus$noMobTick(Maledictus_Entity boss,Operation<Void> original){if(!((Object)this instanceof SpellMaledictus))original.call(boss);}
    @WrapOperation(method="spawnHalberd",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean maledictus$child(Level world,Entity child,Operation<Boolean> original) {
        return (Object)this instanceof SpellMaledictus boss && world instanceof ServerLevel server
                ?MaledictusCasting.child(server,child,boss):original.call(world,child);
    }
    @ModifyArg(method={"aiStep","AreaAttack","ComboAreaAttack","ShieldSmashDamage","Rushattack"},at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/util/CMDamageTypes;causeMaledictioDamage(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;"),index=0)
    private LivingEntity maledictus$owner(LivingEntity original){return (Object)this instanceof SpellMaledictus boss?boss.cast.damageOwner():original;}
    @Inject(method="flyingdestroy",at=@At("HEAD"),cancellable=true)
    private void maledictus$noTerrainDamage(CallbackInfo ci){if((Object)this instanceof SpellMaledictus)ci.cancel();}
    @WrapOperation(method="ShieldSmashDamage",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean maledictus$debris(Level world,Entity child,Operation<Boolean> original) {
        return (Object)this instanceof SpellMaledictus boss && world instanceof ServerLevel server
                ?MaledictusCasting.actionChild(server,child,boss):original.call(world,child);
    }
    @ModifyArg(method={"AreaAttack","ComboAreaAttack","Rushattack","uppercut"},at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=0)
    private LivingEntity maledictus$meleeOwner(LivingEntity original){return (Object)this instanceof SpellMaledictus boss?boss.cast.damageOwner():original;}
    @ModifyArg(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/util/CMDamageTypes;causeMaledictioSoulDamage(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;"),index=0)
    private LivingEntity maledictus$soulOwner(LivingEntity original){return (Object)this instanceof SpellMaledictus boss?boss.cast.damageOwner():original;}
    @ModifyExpressionValue(method={"aiStep","AreaAttack","ComboAreaAttack","Rushattack","uppercut"},at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true))
    private boolean maledictus$refreshAtCap(boolean accepted) {
        // Native setRageMeter is skipped at five levels, but a successful hit still resets decay.
        if(accepted && (Object)this instanceof SpellMaledictus boss && boss.getRageMeter()>=5)
            MaledictusRage.gain(boss.cast.caster(),1);
        return accepted;
    }
}
