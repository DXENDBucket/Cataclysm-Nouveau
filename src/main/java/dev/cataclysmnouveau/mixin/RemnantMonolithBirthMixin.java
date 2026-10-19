package dev.cataclysmnouveau.mixin;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.RemnantCasting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(targets="com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity$RemnantMonolithAttackGoal",remap=false)
public abstract class RemnantMonolithBirthMixin {
    @WrapOperation(method="spawnSpikeLine",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean remnant$birth(Level world,Entity child,Operation<Boolean> original){return RemnantCasting.spawn(child,()->original.call(world,child));}
}
