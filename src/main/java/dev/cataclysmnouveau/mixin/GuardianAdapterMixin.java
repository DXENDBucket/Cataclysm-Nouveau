package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import java.util.List;

@Mixin(value=Ender_Guardian_Entity.class,remap=false)
public abstract class GuardianAdapterMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/LLibrary_Boss_Monster;aiStep()V"))
    private void guardian$noMobTick(Ender_Guardian_Entity boss,Operation<Void> original){if(!((Object)this instanceof SpellGuardian))original.call(boss);}
    @ModifyArg(method="AreaAttack",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=0)
    private LivingEntity guardian$damageOwner(LivingEntity nativeOwner){return (Object)this instanceof SpellGuardian boss?boss.cast.damageOwner():nativeOwner;}
    @WrapOperation(method={"AreaAttack","GravityPull"},at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/Ender_Guardian_Entity;getEntityLivingBaseNearby(DDDD)Ljava/util/List;"))
    private List<LivingEntity> guardian$targets(Ender_Guardian_Entity entity,double x,double y,double z,double range,Operation<List<LivingEntity>> original) {
        var list=original.call(entity,x,y,z,range);
        return (Object)this instanceof SpellGuardian boss?list.stream().filter(e->!boss.isAlliedTo(e)).toList():list;
    }
    @WrapOperation(method="GravityPull",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",remap=true))
    private void guardian$syncPull(LivingEntity entity,Vec3 motion,Operation<Void> original){original.call(entity,motion);if((Object)this instanceof SpellGuardian)entity.hurtMarked=true;}
    @WrapOperation(method="spawnFangs",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean guardian$rune(Level world,Entity child,Operation<Boolean> original){return (Object)this instanceof SpellGuardian boss && world instanceof ServerLevel server?GuardianCasting.rune(server,child,boss):original.call(world,child);}
}
