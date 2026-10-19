package dev.cataclysmnouveau.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.LeviathanFormations;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity$LeviathanMineAttackGoal",
        "com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity$LeviathanAbyssBlastPortalAttackGoal"},remap=false)
public abstract class LeviathanFormationGoalMixin {
    @Inject(method="spawnMines",at=@At("HEAD"),cancellable=true,require=0)
    private void leviathan$mineLoaded(double x,double y,double z,float yaw,int delay,CallbackInfo ci){if(!LeviathanFormations.loaded(x,y,z))ci.cancel();}
    @Inject(method="spawnUnderPortal",at=@At("HEAD"),cancellable=true,require=0)
    private void leviathan$portalLoaded(double x,double z,double min,double max,float yaw,int delay,CallbackInfo ci){if(!LeviathanFormations.loaded(x,max,z))ci.cancel();}
    @WrapOperation(method={"spawnMines","spawnUnderPortal"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true),require=0)
    private boolean leviathan$spawn(Level world,Entity child,Operation<Boolean> original){return LeviathanFormations.spawn(world,child,()->original.call(world,child));}
}
