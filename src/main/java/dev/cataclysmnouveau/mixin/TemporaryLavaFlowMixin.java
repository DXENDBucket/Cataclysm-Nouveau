package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.TemporaryLava;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(FlowingFluid.class)
public abstract class TemporaryLavaFlowMixin {
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$sourceOnly(Level level,BlockPos pos,FluidState fluid,CallbackInfo ci) {
        if(TemporaryLava.contains(level,pos)) ci.cancel();
    }
}
