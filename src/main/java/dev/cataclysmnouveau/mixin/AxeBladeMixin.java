package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Axe_Blade_Entity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.function.Predicate;

@Mixin(value=Axe_Blade_Entity.class,remap=false)
public abstract class AxeBladeMixin {
    @Unique private static final EntityDataAccessor<Integer> aptr$MODE=SynchedEntityData.defineId(Axe_Blade_Entity.class,EntityDataSerializers.INT);
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void aptr$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(aptr$MODE,0);}
    public int confluxCataclysm$mode(){return ((Axe_Blade_Entity)(Object)this).getEntityData().get(aptr$MODE);}
    public void confluxCataclysm$mode(int mode){((Axe_Blade_Entity)(Object)this).getEntityData().set(aptr$MODE,mode);}
    @WrapMethod(method="tick")
    private void aptr$visualTick(Operation<Void> original){
        var blade=(Axe_Blade_Entity)(Object)this;var state=(ManagedProjectile)blade;
        if(state.confluxCataclysm$mode()==1){
            if(state.confluxCataclysm$repeated())blade.lifetick=0;
            if(blade.level().isClientSide)blade.setPos(blade.position().add(blade.getDeltaMovement()));
        }
        original.call();
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;",remap=true))
    private HitResult aptr$collision(Entity e,Predicate<Entity> filter,Operation<HitResult> original){return confluxCataclysm$mode()==1?BlockHitResult.miss(e.position(),net.minecraft.core.Direction.DOWN,e.blockPosition()):original.call(e,filter);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/projectile/Axe_Blade_Entity;setPos(DDD)V",remap=true))
    private void aptr$move(Axe_Blade_Entity e,double x,double y,double z,Operation<Void> original){if(confluxCataclysm$mode()!=1)original.call(e,x,y,z);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/projectile/Axe_Blade_Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",remap=true))
    private void aptr$velocity(Axe_Blade_Entity e,Vec3 v,Operation<Void> original){if(confluxCataclysm$mode()!=1)original.call(e,v);}
    @Inject(method="onHit",at=@At("HEAD"),cancellable=true)
    private void aptr$contact(HitResult hit,CallbackInfo ci){
        var state=(ManagedProjectile)this;
        if(state.confluxCataclysm$mode()==2 && !state.confluxCataclysm$resolving() && state.confluxCataclysm$impact()!=null){state.confluxCataclysm$impact().accept(hit);ci.cancel();}
    }
}
