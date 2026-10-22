package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Void_Scatter_Arrow_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Scatter Arrow inherits vanilla arrow impact; its concrete class has no hurt call to wrap. */
@Mixin(AbstractArrow.class)
public abstract class VoidArrowDamageMixin {
    @WrapOperation(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean voidPayload$damage(Entity victim,DamageSource source,float damage,Operation<Boolean> original){
        var self=(Entity)(Object)this;var cast=NativeDamageContexts.context(self);
        if(!(self instanceof Void_Scatter_Arrow_Entity) || cast==null || self.level().isClientSide)return original.call(victim,source,damage);
        if(HarbingerCasting.allied(cast,victim))return false;
        return NativeSpellDamage.hurt(cast,victim,source,Resources.augmentedDamage(damage,NativeDamageContexts.amplification(self)),(s,a)->original.call(victim,s,a));
    }
}
