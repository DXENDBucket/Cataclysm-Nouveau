package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Arrow_Entity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Phantom_Arrow_Entity.class,remap=false)
public abstract class PhantomArrowStateMixin extends AbstractArrow {
    @Unique private static final EntityDataAccessor<Integer> confluxMaledictus$MODE=SynchedEntityData.defineId(Phantom_Arrow_Entity.class,EntityDataSerializers.INT);
    @Shadow private boolean stopSeeking;
    protected PhantomArrowStateMixin(EntityType<? extends AbstractArrow> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void maledictus$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(confluxMaledictus$MODE,0);}
    public int confluxCataclysm$mode(){return getEntityData().get(confluxMaledictus$MODE);}
    public void confluxCataclysm$mode(int value){getEntityData().set(confluxMaledictus$MODE,value);}
    @WrapMethod(method="tick")
    private void maledictus$visualTick(Operation<Void> original) {
        if(confluxCataclysm$mode()!=1){original.call();return;}
        if(level().isClientSide)setPos(position().add(getDeltaMovement()));
        // AbstractArrow base movement is suppressed by ManagedMovementMixin; native tails still run.
        HarbingerCasting.decorate(this,()->original.call());
    }
    @Inject(method="onHit",at=@At("RETURN"))
    private void maledictus$repeat(HitResult hit,CallbackInfo ci) {
        var state=(ManagedProjectile)this;
        if(confluxCataclysm$mode()==1 && state.confluxCataclysm$repeated()) {
            inGround=false;inGroundTime=0;shakeTime=0;stopSeeking=false;
            ((Phantom_Arrow_Entity)(Object)this).setTransparency(0);
        }
    }
}
