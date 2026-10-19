package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ancient_Desert_Stele_Entity;
import dev.cataclysmnouveau.casting.NativeDamageContexts;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
@Mixin(value={Sandstorm_Entity.class,Ancient_Desert_Stele_Entity.class},remap=false)
public abstract class RemnantAreaMixin extends Entity {
    protected RemnantAreaMixin(EntityType<?> type,Level world){super(type,world);}
    @Override public boolean shouldBeSaved(){return NativeDamageContexts.context(this)==null && super.shouldBeSaved();}
}
