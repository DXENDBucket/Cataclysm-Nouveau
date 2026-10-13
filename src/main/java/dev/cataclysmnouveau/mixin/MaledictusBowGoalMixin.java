package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(targets="com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity$Maledictus_Flying_Bow",remap=false)
public abstract class MaledictusBowGoalMixin {
    @Shadow(remap=false) @Final private Maledictus_Entity entity;
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean maledictus$arrow(Level world,Entity child,Operation<Boolean> original) {
        return entity instanceof SpellMaledictus frame && world instanceof ServerLevel server
                ?MaledictusCasting.actionChild(server,child,frame):original.call(world,child);
    }
}
