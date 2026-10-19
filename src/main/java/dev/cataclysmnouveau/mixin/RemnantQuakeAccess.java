package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.EarthQuake_Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(value=EarthQuake_Entity.class,remap=false)
public interface RemnantQuakeAccess {
    @Invoker("onUpdateInAir") void confluxRemnant$contact();
}
