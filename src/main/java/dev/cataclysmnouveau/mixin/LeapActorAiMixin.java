package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.ScyllaActions;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
/** Temporarily yield movement control during a hook action; never changes the mob's NoAI flag. */
@Mixin(Mob.class)
public abstract class LeapActorAiMixin {
    @Inject(method="serverAiStep",at=@At("HEAD"),cancellable=true)
    private void nativeArs$controlledMotion(CallbackInfo ci) {
        if(ScyllaActions.isLeaping((Mob)(Object)this) || dev.cataclysmnouveau.casting.HarbingerActions.isCharging((Mob)(Object)this) || dev.cataclysmnouveau.casting.GuardianCasting.isPunching((Mob)(Object)this)) ci.cancel();
    }
}
