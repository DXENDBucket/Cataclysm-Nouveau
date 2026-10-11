package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Lionfish_Spike_Entity;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
@Mixin(value=Lionfish_Spike_Entity.class,remap=false)
public abstract class SpikeDamageMixin {
    @ModifyArg(method="onHitEntity",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),index=1)
    private float confluxCataclysm$damage(float nativeAmount) {
        var state=(ManagedProjectile)this;
        return state.confluxCataclysm$mode()==0?nativeAmount:Math.max(0,nativeAmount+state.confluxCataclysm$bonus());
    }
}
