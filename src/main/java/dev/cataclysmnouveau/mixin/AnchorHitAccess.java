package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Player_Ceraunus_Entity;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=Player_Ceraunus_Entity.class,remap=false)
public interface AnchorHitAccess {
    @Invoker("onHitBlock") void confluxCataclysm$blockVisual(BlockHitResult hit);
}
