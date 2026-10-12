package dev.cataclysmnouveau.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(targets={"com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity$LaunchGoal","com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity$MissileLaunchGoal","com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity$MissileLaunchGoal2"},remap=false)
public abstract class HarbingerVolleyGoalMixin {
    @WrapOperation(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true),require=1)
    private boolean harbinger$child(Level world,Entity child,Operation<Boolean> original) {
        return world instanceof ServerLevel server && child instanceof Projectile p && p.getOwner() instanceof SpellHarbinger frame
                ?HarbingerActions.child(server,child,frame,()->original.call(world,child)):original.call(world,child);
    }
}
