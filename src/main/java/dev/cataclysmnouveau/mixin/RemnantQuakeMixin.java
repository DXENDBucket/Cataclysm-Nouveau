package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.EarthQuake_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=EarthQuake_Entity.class,remap=false)
public abstract class RemnantQuakeMixin {
    @Unique private static final EntityDataAccessor<Integer> remnant$MODE=SynchedEntityData.defineId(EarthQuake_Entity.class,EntityDataSerializers.INT);
    @Shadow(remap=false) private int lifeTime;
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void remnant$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(remnant$MODE,0);}
    public int confluxCataclysm$mode(){return ((EarthQuake_Entity)(Object)this).getEntityData().get(remnant$MODE);}
    public void confluxCataclysm$mode(int mode){((EarthQuake_Entity)(Object)this).getEntityData().set(remnant$MODE,mode);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/projectile/EarthQuake_Entity;move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V",remap=true))
    private void remnant$move(EarthQuake_Entity wave,MoverType type,Vec3 movement,Operation<Void> original){if(confluxCataclysm$mode()!=1)original.call(wave,type,movement);}
    @Inject(method="onUpdateInAir",at=@At("TAIL"))
    private void remnant$carrierLifetime(CallbackInfo ci){if(confluxCataclysm$mode()==1)lifeTime=60;}
    @WrapOperation(method="onUpdateInAir",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z",remap=true))
    private boolean remnant$hit(LivingEntity target,DamageSource source,float amount,Operation<Boolean> original){
        return RemnantQuake.damage((EarthQuake_Entity)(Object)this,target,source,amount,(s,a)->original.call(target,s,a));
    }
}
