package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.CustomAbstractHurtingProjectile;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=CustomAbstractHurtingProjectile.class,remap=false)
public abstract class SpearMovementMixin {
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void confluxScylla$movement(CallbackInfo ci) {
        if(!((Object)this instanceof ManagedProjectile state) || state.confluxCataclysm$mode()!=1) return;
        var spear=(CustomAbstractHurtingProjectile)(Object)this;spear.baseTick();
        if(spear.level().isClientSide) {
            spear.setPos(spear.position().add(spear.getDeltaMovement()));
            ProjectileUtil.rotateTowardsMovement(spear,1);
            ((SpearVisualAccess)this).confluxScylla$trail();
        }
        ci.cancel(); // Elemental_Spear_Entity.tick still advances its native spawn animation.
    }
}
