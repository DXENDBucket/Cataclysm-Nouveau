package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Player_Ceraunus_Entity;
import dev.cataclysmnouveau.casting.ManagedProjectile;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraft.core.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Player_Ceraunus_Entity.class,remap=false)
public abstract class AnchorStateMixin extends AbstractArrow {
    @Unique private static final EntityDataAccessor<Integer> confluxCataclysm$MODE=SynchedEntityData.defineId(Player_Ceraunus_Entity.class,EntityDataSerializers.INT);
    protected AnchorStateMixin(EntityType<? extends AbstractArrow> type,Level level) {super(type,level);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void confluxCataclysm$define(SynchedEntityData.Builder builder,CallbackInfo ci) {builder.define(confluxCataclysm$MODE,0);}
    public int confluxCataclysm$mode() {return entityData.get(confluxCataclysm$MODE);}
    public void confluxCataclysm$mode(int mode) {entityData.set(confluxCataclysm$MODE,mode);}
    @Inject(method="onHit",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$hit(HitResult hit,CallbackInfo ci) {
        var state=(ManagedProjectile)this;
        if(state.confluxCataclysm$mode()==0) return;
        if(level().isClientSide || ((Player_Ceraunus_Entity)(Object)this).getReturn()) {ci.cancel();return;}
        if(state.confluxCataclysm$mode()==2 && !state.confluxCataclysm$resolving() && state.confluxCataclysm$impact()!=null) {
            state.confluxCataclysm$impact().accept(hit);ci.cancel();
        }
    }
    @Inject(method="setReturn",at=@At("HEAD"),cancellable=true)
    private void confluxCataclysm$keepOrbit(boolean value,CallbackInfo ci) {
        var state=(ManagedProjectile)this;
        if(value && state.confluxCataclysm$mode()==1 && state.confluxCataclysm$repeated()) ci.cancel();
    }
    @Override public void handleEntityEvent(byte event) {
        if(event==73 && level().isClientSide && ((ManagedProjectile)this).confluxCataclysm$mode()!=0) {
            // Dispatch dynamically to Cataclysm's own client particles, not a copied particle implementation.
            ((AnchorHitAccess)this).confluxCataclysm$blockVisual(new BlockHitResult(position(),Direction.UP,BlockPos.containing(position()),false));
        } else super.handleEntityEvent(event);
    }
}
