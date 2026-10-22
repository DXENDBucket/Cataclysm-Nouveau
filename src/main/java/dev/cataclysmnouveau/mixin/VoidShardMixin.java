package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Void_Shard_Entity;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.HarbingerCasting;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Void_Shard_Entity.class,remap=false)
public abstract class VoidShardMixin extends ThrowableItemProjectile {
    protected VoidShardMixin(EntityType<? extends ThrowableItemProjectile> type,Level level){super(type,level);}
    @Override public boolean shouldBeSaved(){return (level().isClientSide || NativeSpellDamage.context(this)==null) && super.shouldBeSaved();}
    @Inject(method="onHitEntity",at=@At("HEAD"),cancellable=true)
    private void voidPayload$allies(EntityHitResult hit,CallbackInfo ci){
        if(level().isClientSide)return;
        var cast=NativeSpellDamage.context(this);
        // Reject before the native post-hit invulnerability reset, including an explicitly invoked hit.
        if(cast!=null && HarbingerCasting.allied(cast,hit.getEntity()))ci.cancel();
    }
}
