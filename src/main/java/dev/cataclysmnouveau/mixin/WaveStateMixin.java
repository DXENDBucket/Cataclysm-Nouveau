package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.effect.Wave_Entity;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Wave_Entity.class,remap=false)
public abstract class WaveStateMixin implements WaveControl {
    @Unique private static final EntityDataAccessor<Integer> confluxCataclysm$MODE=SynchedEntityData.defineId(Wave_Entity.class,EntityDataSerializers.INT);
    @Unique private static final EntityDataAccessor<Float> confluxCataclysm$SPEED=SynchedEntityData.defineId(Wave_Entity.class,EntityDataSerializers.FLOAT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void confluxCataclysm$define(SynchedEntityData.Builder builder,CallbackInfo ci) {builder.define(confluxCataclysm$MODE,0);builder.define(confluxCataclysm$SPEED,1F);}
    public int confluxCataclysm$mode() {return ((Wave_Entity)(Object)this).getEntityData().get(confluxCataclysm$MODE);}
    public void confluxCataclysm$mode(int mode) {((Wave_Entity)(Object)this).getEntityData().set(confluxCataclysm$MODE,mode);}
    public void confluxCataclysm$speed(float multiplier) {((Wave_Entity)(Object)this).getEntityData().set(confluxCataclysm$SPEED,multiplier);}
    @Invoker("attackEntities") public abstract void confluxCataclysm$attack(double strength,double x,double z);
    @Inject(method="attackEntities",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$managedContact(double strength,double x,double z,CallbackInfo ci) {
        var state=(ManagedProjectile)this;
        if(state.confluxCataclysm$mode()==1 && !state.confluxCataclysm$resolving()) ci.cancel();
    }
    @Redirect(method="attackEntities",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean confluxCataclysm$contact(LivingEntity target,DamageSource source,float amount) {
        boolean result=dev.cataclysmnouveau.casting.ArsDamage.hurt(dev.cataclysmnouveau.casting.ArsDamage.context((net.minecraft.world.entity.Entity)(Object)this),target,source,amount,target::hurt);var state=(ManagedProjectile)this;
        if(result && state.confluxCataclysm$mode()==2 && state.confluxCataclysm$impact()!=null)
            state.confluxCataclysm$impact().accept(new EntityHitResult(target));
        return result;
    }
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/effect/Wave_Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V"))
    private void confluxCataclysm$move(Wave_Entity wave,MoverType type,Vec3 movement) {
        if(((ManagedProjectile)this).confluxCataclysm$mode()!=1) wave.move(type,movement);
    }
    @Redirect(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/effect/Wave_Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V"))
    private void confluxCataclysm$velocity(Wave_Entity wave,Vec3 movement) {
        if(((ManagedProjectile)this).confluxCataclysm$mode()!=1) wave.setDeltaMovement(movement);
    }
    @ModifyArg(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/phys/Vec3;add(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;"),index=0)
    private Vec3 confluxCataclysm$accelerate(Vec3 nativeAcceleration) {
        var wave=(Wave_Entity)(Object)this;
        return ((ManagedProjectile)this).confluxCataclysm$mode()==2?nativeAcceleration.scale(wave.getEntityData().get(confluxCataclysm$SPEED)):nativeAcceleration;
    }
}
