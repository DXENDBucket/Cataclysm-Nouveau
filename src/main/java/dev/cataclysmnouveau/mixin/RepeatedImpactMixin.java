package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class RepeatedImpactMixin {
    @Inject(method="discard",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$keepCarrier(CallbackInfo ci) {
        if ((Object)this instanceof ManagedProjectile state && state.confluxCataclysm$mode()==1
            && state.confluxCataclysm$repeated() && state.confluxCataclysm$resolving()) ci.cancel();
    }
}
