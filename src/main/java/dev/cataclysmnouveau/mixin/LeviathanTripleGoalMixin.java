package dev.cataclysmnouveau.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.LeviathanVolleys;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets="com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity$LeviathanBlastFireAttackGoal",remap=false)
public abstract class LeviathanTripleGoalMixin {
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean leviathan$beam(Level world,Entity child,Operation<Boolean> original){return LeviathanVolleys.spawn(world,child,()->original.call(world,child));}
    @WrapOperation(method="start",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",remap=true))
    private void leviathan$positionalSound(Level world,Player except,Entity emitter,SoundEvent sound,SoundSource source,float volume,float pitch,Operation<Void> original) {
        if(!LeviathanVolleys.sound(world,except,emitter,sound,source,volume,pitch))original.call(world,except,emitter,sound,source,volume,pitch);
    }
}
