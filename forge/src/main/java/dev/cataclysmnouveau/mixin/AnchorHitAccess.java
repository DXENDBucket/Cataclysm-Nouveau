package dev.cataclysmnouveau.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Forge's accessor processor does not resolve this inherited mapping on the native
 * anchor class. Resolve it on the vanilla declaring class instead. The generated
 * virtual call still dispatches to Player_Ceraunus_Entity's native override.
 */
@Mixin(AbstractArrow.class)
public interface AnchorHitAccess {
    @Invoker("onHitBlock") void confluxCataclysm$blockVisual(BlockHitResult hit);
}
