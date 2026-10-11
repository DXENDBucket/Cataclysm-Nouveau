package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Elemental_Spear_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=Elemental_Spear_Entity.class,remap=false)
public abstract class SpearStateMixin implements ScyllaPayload {
    @Unique private static final EntityDataAccessor<Integer> confluxScylla$MODE=SynchedEntityData.defineId(Elemental_Spear_Entity.class,EntityDataSerializers.INT);
    @Unique private CastContext confluxScylla$cast;
    public CastContext confluxScylla$cast() {return confluxScylla$cast;}
    public void confluxScylla$cast(CastContext cast) {confluxScylla$cast=cast;}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void confluxScylla$define(SynchedEntityData.Builder builder,CallbackInfo ci) {builder.define(confluxScylla$MODE,0);}
    public int confluxCataclysm$mode() {return ((Entity)(Object)this).getEntityData().get(confluxScylla$MODE);}
    public void confluxCataclysm$mode(int mode) {((Entity)(Object)this).getEntityData().set(confluxScylla$MODE,mode);}
    @Inject(method="onHit",at=@At("HEAD"),cancellable=true)
    private void confluxScylla$hit(HitResult hit,CallbackInfo ci) {
        var state=(ManagedProjectile)this;if(state.confluxCataclysm$mode()==0) return;
        if(((Entity)(Object)this).level().isClientSide) {ci.cancel();return;}
        if(confluxScylla$cast!=null && hit instanceof EntityHitResult entity
                && (Allies.areAllies(confluxScylla$cast.caster(),entity.getEntity()) || Allies.areAllies(confluxScylla$cast.damageOwner(),entity.getEntity()))) {ci.cancel();return;}
        if(!state.confluxCataclysm$resolving() && state.confluxCataclysm$impact()!=null) {
            state.confluxCataclysm$impact().accept(hit);ci.cancel();
        }
    }
    @Inject(method="canHitEntity",at=@At("RETURN"),cancellable=true)
    private void confluxScylla$allies(Entity target,CallbackInfoReturnable<Boolean> ci) {
        if(confluxScylla$cast!=null && (Allies.areAllies(confluxScylla$cast.caster(),target) || Allies.areAllies(confluxScylla$cast.damageOwner(),target))) ci.setReturnValue(false);
    }
}
