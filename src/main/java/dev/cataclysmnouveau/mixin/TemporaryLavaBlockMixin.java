package dev.cataclysmnouveau.mixin;
import dev.cataclysmnouveau.casting.TemporaryLava;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Level.class)
public abstract class TemporaryLavaBlockMixin {
    @Inject(method="setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",at=@At("RETURN"))
    private void confluxCataclysm$removedSource(BlockPos pos,BlockState block,int flags,int depth,CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() && !block.is(Blocks.LAVA)) TemporaryLava.consumed((Level)(Object)this,pos);
    }
}
