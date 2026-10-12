package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.SpellHarbinger;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
/** The native collision attack is invoked separately, without running Boss AI or griefing. */
@Mixin(value=The_Harbinger_Entity.class,remap=false)
public abstract class HarbingerAttackMixin {
    @WrapOperation(method="blockbreak",at=@At(value="INVOKE",target="Lnet/neoforged/neoforge/event/EventHooks;canEntityGrief(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean harbinger$noGrief(Level world,Entity source,Operation<Boolean> original){return !(source instanceof SpellHarbinger) && original.call(world,source);}
    @ModifyArg(method="blockbreak",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobAttack(Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=0)
    private LivingEntity harbinger$owner(LivingEntity original){return (Object)this instanceof SpellHarbinger frame?frame.cast.damageOwner():original;}
}
