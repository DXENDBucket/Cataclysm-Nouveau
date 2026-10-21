package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Axe_Blade_Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=Axe_Blade_Entity.class,remap=false)
public interface AxeBladeAccess {
    @Invoker("onHit") void confluxAptrgangr$hit(HitResult hit);
}
