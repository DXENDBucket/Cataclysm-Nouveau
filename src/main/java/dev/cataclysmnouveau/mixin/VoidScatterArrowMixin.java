package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Void_Scatter_Arrow_Entity;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Void_Scatter_Arrow_Entity.class,remap=false)
public abstract class VoidScatterArrowMixin extends AbstractArrow {
    @Unique private static final EntityDataAccessor<Integer> voidPayload$MODE=SynchedEntityData.defineId(Void_Scatter_Arrow_Entity.class,EntityDataSerializers.INT);
    protected VoidScatterArrowMixin(EntityType<? extends AbstractArrow> type,Level level){super(type,level);}
    @Override protected void defineSynchedData(SynchedEntityData.Builder builder){super.defineSynchedData(builder);builder.define(voidPayload$MODE,0);}
    public int confluxCataclysm$mode(){return getEntityData().get(voidPayload$MODE);}
    public void confluxCataclysm$mode(int value){getEntityData().set(voidPayload$MODE,value);}
    @WrapMethod(method="onHit")
    private void voidPayload$impactMotion(HitResult hit,Operation<Void> original){
        var state=(ManagedProjectile)this;var velocity=getDeltaMovement();
        original.call(hit);
        if(state.confluxCataclysm$mode()==1 && state.confluxCataclysm$repeated())setDeltaMovement(velocity);
    }
    @WrapOperation(method="onHit",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean voidPayload$shards(Level level,Entity child,Operation<Boolean> original){return VoidPayloads.child(this,child,()->original.call(level,child));}
    @WrapOperation(method="onHit",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",remap=true))
    private void voidPayload$oneShatter(Level world,net.minecraft.core.particles.ParticleOptions particle,double x,double y,double z,double dx,double dy,double dz,Operation<Void> original){
        if(((ManagedProjectile)this).confluxCataclysm$mode()==0)original.call(world,particle,x,y,z,dx,dy,dz);
    }
    @Inject(method="onHit",at=@At("RETURN"))
    private void voidPayload$repeat(HitResult hit,CallbackInfo ci){
        var state=(ManagedProjectile)this;
        if(!(level() instanceof ServerLevel server) || NativeSpellDamage.context(this)==null
                || state.confluxCataclysm$mode()==2 && !state.confluxCataclysm$resolving())return;
        // The native shatter branch runs only on a predicting client; Ars-controlled forms have no such hit.
        server.sendParticles(new ItemParticleOption(ParticleTypes.ITEM,new ItemStack(ModItems.VOID_SCATTER_ARROW.get())),getX(),getY(),getZ(),8,.1,.15,.1,.1);
        if(state.confluxCataclysm$mode()==1 && state.confluxCataclysm$repeated()){inGround=false;inGroundTime=0;shakeTime=0;}
    }
}
