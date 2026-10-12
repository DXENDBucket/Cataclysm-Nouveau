package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import java.util.List;
@Mixin(value=Ignis_Entity.class,remap=false)
public abstract class IgnisAdapterMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/LLibrary_Boss_Monster;aiStep()V"))
    private void ignis$noMobTick(Ignis_Entity boss,Operation<Void> original){if(!((Object)this instanceof SpellIgnis))original.call(boss);}
    @ModifyArg(method={"AreaAttack","BodyCheckAttack","ShieldSmashDamage","UltimateAttack","aiStep"},at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=0)
    private LivingEntity ignis$owner(LivingEntity owner){return (Object)this instanceof SpellIgnis boss?boss.cast.damageOwner():owner;}
    @WrapOperation(method={"AreaAttack","BodyCheckAttack"},at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/Ignis_Entity;getEntityLivingBaseNearby(DDDD)Ljava/util/List;"))
    private List<LivingEntity> ignis$targets(Ignis_Entity entity,double x,double y,double z,double range,Operation<List<LivingEntity>> original) {
        var targets=original.call(entity,x,y,z,range);
        return (Object)this instanceof SpellIgnis boss?targets.stream().filter(e->!boss.isAlliedTo(e)).toList():targets;
    }
    @WrapOperation(method={"AreaAttack","BodyCheckAttack","ShieldSmashDamage","UltimateAttack"},at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",remap=true),require=0)
    private void ignis$motion(LivingEntity entity,Vec3 motion,Operation<Void> original){original.call(entity,motion);if((Object)this instanceof SpellIgnis)entity.hurtMarked=true;}
    @WrapOperation(method={"bladeFireball","shootFireball","shootAbyssFireball","spawnFlameStrike"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean ignis$child(Level world,Entity child,Operation<Boolean> original){return (Object)this instanceof SpellIgnis boss && world instanceof ServerLevel server?IgnisCasting.child(server,child,boss,()->original.call(world,child)):original.call(world,child);}
    @WrapOperation(method={"ShieldSmashDamage","UltimateAttack"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean ignis$noGrief(Level world,BlockPos pos,boolean drop,Entity actor,Operation<Boolean> original){return !((Object)this instanceof SpellIgnis) && original.call(world,pos,drop,actor);}
}
