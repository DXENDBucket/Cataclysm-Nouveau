package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.CustomAbstractHurtingProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value=CustomAbstractHurtingProjectile.class,remap=false)
public interface SpearVisualAccess {
    @Invoker("TrailParticle") void confluxScylla$trail();
}
