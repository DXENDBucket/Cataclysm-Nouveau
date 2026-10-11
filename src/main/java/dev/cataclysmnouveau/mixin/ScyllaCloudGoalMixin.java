package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(targets="com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity$ThunderCloud",remap=false)
public abstract class ScyllaCloudGoalMixin {
    @Shadow @Final private Scylla_Entity entity;
    @ModifyArg(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),index=0)
    private Entity confluxScylla$child(Entity child) {
        return entity instanceof SpellScylla frame?ScyllaCasting.bossChild(child,frame):child;
    }
}
