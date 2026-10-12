package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Suppress only native movement inside a managed decorative tick, never trajectory updates. */
@Mixin(Entity.class)
public abstract class HarbingerDecorationMixin {
    @Inject(method="setPos(DDD)V",at=@At("HEAD"),cancellable=true)
    private void harbinger$position(double x,double y,double z,CallbackInfo ci){if(HarbingerCasting.decorating((Entity)(Object)this))ci.cancel();}
    @Inject(method="setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",at=@At("HEAD"),cancellable=true)
    private void harbinger$motion(Vec3 v,CallbackInfo ci){if(HarbingerCasting.decorating((Entity)(Object)this))ci.cancel();}
    @Inject(method="discard",at=@At("HEAD"),cancellable=true)
    private void harbinger$expiry(CallbackInfo ci){var e=(Entity)(Object)this;if(HarbingerCasting.decorating(e) && !((ManagedProjectile)e).confluxCataclysm$resolving())ci.cancel();}
}
