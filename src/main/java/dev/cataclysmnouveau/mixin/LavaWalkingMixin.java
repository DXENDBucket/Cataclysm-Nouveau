package dev.cataclysmnouveau.mixin;

import dev.cataclysmnouveau.CataclysmEffects;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Fluid collision and buoyancy are opt-in and driven on both sides by the synced effect. */
@Mixin(LivingEntity.class)
public abstract class LavaWalkingMixin {
    @Inject(method="canStandOnFluid",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$lavaSurface(FluidState fluid,CallbackInfoReturnable<Boolean> ci) {
        var actor=(LivingEntity)(Object)this;
        if(fluid.is(FluidTags.LAVA) && actor.hasEffect(CataclysmEffects.LAVA_WALKING) && !actor.isShiftKeyDown())
            ci.setReturnValue(true);
    }
    @Inject(method="tick",at=@At("TAIL"))
    private void confluxCataclysm$buoyancy(CallbackInfo ci) {
        var actor=(LivingEntity)(Object)this;
        if(!actor.hasEffect(CataclysmEffects.LAVA_WALKING)) return;
        if(actor.isInLava()) {actor.clearFire();actor.fallDistance=0;}
        if(actor.isShiftKeyDown() || actor.isPassenger() || actor.isNoGravity()) return;
        var at=actor.blockPosition();
        var fluid=actor.level().getFluidState(at);
        if(!fluid.is(FluidTags.LAVA)) {at=at.below();fluid=actor.level().getFluidState(at);}
        if(fluid.is(FluidTags.LAVA)) {
            double surface=at.getY()+fluid.getHeight(actor.level(),at);
            var motion=actor.getDeltaMovement();
            if(actor.getY()>=surface-.125 && actor.getY()<=surface+.125 && motion.y<=.12
                    && !actor.level().getFluidState(at.above()).is(FluidTags.LAVA)) {
                actor.setOnGround(true);
                actor.fallDistance=0;
                // Stop the small buoyancy impulse at the surface; preserve a real jump's larger velocity.
                if(motion.y>0 && motion.y<=.12) actor.setDeltaMovement(motion.x,0,motion.z);
            } else if(actor.isInLava() && actor.getY()<surface-.01) actor.setDeltaMovement(motion.x,Math.max(motion.y,.12),motion.z);
        }
    }
}
