package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value=Spark_Entity.class,remap=false)
public abstract class SparkChildMixin {
    @ModifyArg(method={"onHitBlock","spawnArea"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),index=0,require=1)
    private Entity confluxScylla$inherit(Entity child) {
        var cast=((ScyllaPayload)this).confluxScylla$cast();
        return cast!=null && child instanceof ScyllaPayload?ScyllaCasting.manage(child,cast):child;
    }
}
