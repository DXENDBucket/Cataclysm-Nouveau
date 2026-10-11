package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.world.entity.projectile.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(ThrowableProjectile.class)
public abstract class ManagedMovementMixin {
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$movement(CallbackInfo ci) {
        if (!((Object)this instanceof ManagedProjectile state) || state.confluxCataclysm$mode()!=1) return;
        var projectile=(Projectile)(Object)this;projectile.baseTick();
        if (projectile.level().isClientSide) {
            projectile.setPos(projectile.position().add(projectile.getDeltaMovement()));
            ProjectileUtil.rotateTowardsMovement(projectile,.2F);
        }
        ci.cancel();
    }
}
