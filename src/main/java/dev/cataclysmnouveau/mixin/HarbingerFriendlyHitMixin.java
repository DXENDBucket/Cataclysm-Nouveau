package dev.cataclysmnouveau.mixin;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.HarbingerCasting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Projectile.class)
public abstract class HarbingerFriendlyHitMixin {
    @Inject(method="canHitEntity",at=@At("HEAD"),cancellable=true)
    private void harbinger$allies(Entity target,CallbackInfoReturnable<Boolean> ci){var e=(Entity)(Object)this;if(e.level().isClientSide)return;var c=NativeSpellDamage.context(e);if(HarbingerCasting.nativePayload(e) && c!=null && HarbingerCasting.allied(c,target))ci.setReturnValue(false);}
}
