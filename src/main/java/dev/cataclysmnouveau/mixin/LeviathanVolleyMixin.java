package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.sounds.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=The_Leviathan_Entity.class,remap=false)
public abstract class LeviathanVolleyMixin {
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/LLibrary_Boss_Monster;aiStep()V"))
    private void leviathan$noMobTick(The_Leviathan_Entity boss,Operation<Void> original){if(!((Object)this instanceof SpellLeviathan))original.call(boss);}
    @WrapOperation(method="shootAbyssOrb",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean leviathan$orb(Level world,Entity child,Operation<Boolean> original){return LeviathanVolleys.spawn(world,child,()->original.call(world,child));}
    @WrapOperation(method="aiStep",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V",remap=true))
    private void leviathan$positionalSound(Level world,Player except,Entity emitter,SoundEvent sound,SoundSource source,float volume,float pitch,Operation<Void> original) {
        if(!LeviathanVolleys.sound(world,except,emitter,sound,source,volume,pitch))original.call(world,except,emitter,sound,source,volume,pitch);
    }
}
