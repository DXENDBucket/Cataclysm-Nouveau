package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.CMAbstractHurtingProjectile;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.HarbingerCasting;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Predicate;
/** Skip native ray collision only during a special Ars trajectory's decorative tick. */
@Mixin(value=CMAbstractHurtingProjectile.class,remap=false)
public abstract class IgnisMotionMixin {
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;Lnet/minecraft/world/level/ClipContext$Block;)Lnet/minecraft/world/phys/HitResult;",remap=true))
    private HitResult ignis$collision(Entity entity,Predicate<Entity> filter,ClipContext.Block clip,Operation<HitResult> original) {
        return HarbingerCasting.decorating(entity)?BlockHitResult.miss(entity.position(),Direction.DOWN,entity.blockPosition()):original.call(entity,filter,clip);
    }
}
