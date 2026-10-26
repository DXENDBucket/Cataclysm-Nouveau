package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.util.CustomExplosion.IgnisExplosion;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.ForgeExplosionCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
@Mixin(value={Explosion.class,IgnisExplosion.class})
public abstract class ForgeExplosionMixin {
    @WrapOperation(method="explode",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean confluxCataclysm$damage(Entity target,DamageSource source,float amount,Operation<Boolean> original){
        var boom=(Explosion)(Object)this;
        if(((ForgeExplosionAccess)this).confluxCataclysm$calculator() instanceof ForgeExplosionCalculator calculator){
            if(!calculator.shouldDamageEntity(boom,target))return false;
            amount=calculator.adjust(boom,target,amount);
        }
        return original.call(target,source,amount);
    }
}
