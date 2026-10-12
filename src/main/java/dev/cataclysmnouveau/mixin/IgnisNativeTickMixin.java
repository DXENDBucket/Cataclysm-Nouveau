package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.damagesource.DamageSource;
/** Run the full native tick for model/trail history, while retaining one motion driver. */
@Mixin(value={Ignis_Fireball_Entity.class,Ignis_Abyss_Fireball_Entity.class},remap=false)
public abstract class IgnisNativeTickMixin implements IgnisProjectile {
    @Shadow(remap=false) private int timer;
    @Unique private LivingEntity ignis$target;
    @Unique private double ignis$acceleration=1;
    public void confluxIgnis$aim(LivingEntity target,double acceleration){ignis$target=target;ignis$acceleration=acceleration;}
    public boolean confluxIgnis$ready(){return (Object)this instanceof Ignis_Fireball_Entity fire?fire.getFired():((Ignis_Abyss_Fireball_Entity)(Object)this).getFired();}
    @WrapMethod(method="tick")
    private void ignis$visualTick(Operation<Void> original) {
        var entity=(CMAbstractHurtingProjectile)(Object)this;
        if(((ManagedProjectile)entity).confluxCataclysm$mode()!=1){original.call();return;}
        if(entity.level().isClientSide)entity.setPos(entity.position().add(entity.getDeltaMovement()));
        HarbingerCasting.decorate(entity,()->original.call());
    }
    @Inject(method="tick",at=@At("RETURN"))
    private void ignis$selectedTarget(CallbackInfo ci) {
        var entity=(CMAbstractHurtingProjectile)(Object)this;
        if(entity.level().isClientSide || ((ManagedProjectile)entity).confluxCataclysm$mode()!=2)return;
        if(timer==0 || timer==-40)IgnisCasting.steer(entity,ignis$target,ignis$acceleration);
    }
    @Inject(method="hurt",at=@At("RETURN"))
    private void ignis$keepCastOwner(DamageSource source,float damage,CallbackInfoReturnable<Boolean> ci) {
        var shot=(CMAbstractHurtingProjectile)(Object)this;
        if(shot.level().isClientSide || !ci.getReturnValue() || ((ManagedProjectile)shot).confluxCataclysm$mode()==0)return;
        var cast=NativeDamageContexts.context(shot);
        // Native deflection redirects flight without silently replacing the cast's damage role.
        if(cast!=null)shot.setOwner(cast.damageOwner());
    }
}
