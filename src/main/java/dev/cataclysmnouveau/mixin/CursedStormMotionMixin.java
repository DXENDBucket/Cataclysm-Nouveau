package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.CustomAbstractHurtingProjectile;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.HarbingerCasting;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Predicate;

/** Custom Cataclysm base owns collision before its concrete subclass tick. */
@Mixin(value=CustomAbstractHurtingProjectile.class,remap=false)
public abstract class CursedStormMotionMixin {
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lnet/minecraft/world/phys/HitResult;",remap=true))
    private HitResult remnant$collision(Entity e,Predicate<Entity> filter,ClipContext.Block type,Operation<HitResult> original){
        return HarbingerCasting.decorating(e)?BlockHitResult.miss(e.position(),Direction.DOWN,e.blockPosition()):original.call(e,filter,type);
    }
}
