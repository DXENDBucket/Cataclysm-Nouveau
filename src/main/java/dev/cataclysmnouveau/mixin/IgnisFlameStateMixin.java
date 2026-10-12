package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity;
import dev.cataclysmnouveau.casting.NativeDamageContexts;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/** Keep all native synced waiting/visibility/radius fields and client tick behavior. */
@Mixin(value=Flame_Strike_Entity.class,remap=false)
public abstract class IgnisFlameStateMixin extends Entity {
    protected IgnisFlameStateMixin(EntityType<?> type,Level world){super(type,world);}
    @Override public boolean shouldBeSaved(){return NativeDamageContexts.context(this)==null && super.shouldBeSaved();}
}
