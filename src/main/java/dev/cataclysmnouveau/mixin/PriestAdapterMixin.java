package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.Deepling.Deepling_Priest_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.SpellPriest;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Native priests remain unchanged; only the unspawned adapter skips its ordinary mob tick. */
@Mixin(value=Deepling_Priest_Entity.class,remap=false)
public abstract class PriestAdapterMixin {
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/Deepling/AbstractDeepling;tick()V"))
    private void priest$frameOnly(Deepling_Priest_Entity entity,Operation<Void> original){
        if(!((Object)this instanceof SpellPriest))original.call(entity);
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;indirectMagic(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true))
    private DamageSource priest$realOwner(DamageSources sources,Entity direct,Entity owner,Operation<DamageSource> original){
        if((Object)this instanceof SpellPriest frame){
            var cast=frame.cast;owner=cast.damageOwner()==null?cast.caster():cast.damageOwner();
            direct=cast.executor()==null?owner:cast.executor();
        }
        return original.call(sources,direct,owner);
    }
}
