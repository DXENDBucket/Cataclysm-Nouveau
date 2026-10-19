package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.items.Tidal_Claws;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.LeviathanTentacles;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=Tidal_Claws.class,remap=false)
public abstract class TidalClawsCastMixin {
    @WrapOperation(method="launchTendonsAt",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean leviathan$initial(Level world,Entity child,Operation<Boolean> original){return LeviathanTentacles.initial(child,()->original.call(world,child));}
    @WrapOperation(method="launchTendonsAt",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/util/TidalTentacleUtil;setLastTentacle(Lnet/minecraft/world/entity/LivingEntity;Lcom/github/L_Ender/cataclysm/entity/projectile/Tidal_Tentacle_Entity;)V"))
    private void leviathan$pointer(LivingEntity owner,Tidal_Tentacle_Entity segment,Operation<Void> original){
        original.call(owner,segment);
        if(LeviathanTentacles.starting(owner))LeviathanTentacles.pointerMetadata(owner,segment);
    }
}
