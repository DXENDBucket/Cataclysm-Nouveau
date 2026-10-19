package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignited_Revenant_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value=Ignited_Revenant_Entity.class,remap=false)
public abstract class RevenantAdapterMixin {
    @WrapOperation(method={"launchbone1","launchbone2","launchbone3"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean revenant$child(Level world,Entity child,Operation<Boolean> original){
        return (Object)this instanceof SpellRevenant frame?RevenantCasting.birth(frame,child,()->original.call(world,child)):original.call(world,child);
    }
}
