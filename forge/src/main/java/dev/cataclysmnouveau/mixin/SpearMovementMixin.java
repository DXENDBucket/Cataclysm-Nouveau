package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Elemental_Spear_Entity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import java.util.function.Predicate;
/** Forge owns spear visuals in the concrete tick; preserve that complete tick. */
@Mixin(value=Elemental_Spear_Entity.class,remap=false)
public abstract class SpearMovementMixin {
 @WrapMethod(method="tick",remap=true)
 private void confluxSpear$visuals(Operation<Void> original){
  var e=(Entity)(Object)this;
  if(((ManagedProjectile)e).confluxCataclysm$mode()!=1){original.call();return;}
  if(e.level().isClientSide)e.setPos(e.position().add(e.getDeltaMovement()));
  HarbingerCasting.decorate(e,()->original.call());
 }
 @WrapOperation(method="tick",remap=true,at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/projectile/ProjectileUtil;getHitResultOnMoveVector(Lnet/minecraft/world/entity/Entity;Ljava/util/function/Predicate;)Lnet/minecraft/world/phys/HitResult;"))
 private HitResult confluxSpear$collision(Entity e,Predicate<Entity> filter,Operation<HitResult> original){
  return HarbingerCasting.decorating(e)?BlockHitResult.miss(e.position(),Direction.DOWN,e.blockPosition()):original.call(e,filter);
 }
}
