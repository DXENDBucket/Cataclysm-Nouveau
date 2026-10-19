package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Cursed_Sandstorm_Entity;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Cursed_Sandstorm_Entity.class,remap=false)
public abstract class CursedStormMixin implements CursedStorm {
    @Unique private static final EntityDataAccessor<Integer> remnant$MODE=SynchedEntityData.defineId(Cursed_Sandstorm_Entity.class,EntityDataSerializers.INT);
    @Unique private double remnant$acceleration=1;
    @Shadow(remap=false) private int lifeTicks;
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void remnant$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(remnant$MODE,0);}
    public int confluxCataclysm$mode(){return ((Cursed_Sandstorm_Entity)(Object)this).getEntityData().get(remnant$MODE);}
    public void confluxCataclysm$mode(int mode){((Cursed_Sandstorm_Entity)(Object)this).getEntityData().set(remnant$MODE,mode);}
    public void confluxRemnant$acceleration(double scale){remnant$acceleration=scale;}
    @ModifyConstant(method="tickHoming",constant=@Constant(doubleValue=.075))
    private double remnant$homingPositive(double original){return original*remnant$acceleration;}
    @ModifyConstant(method="tickHoming",constant=@Constant(doubleValue=-.075))
    private double remnant$homingNegative(double original){return original*remnant$acceleration;}
    @Inject(method="tick",at=@At("HEAD"))
    private void remnant$repeatingLifetime(CallbackInfo ci){var state=(ManagedProjectile)this;if(state.confluxCataclysm$mode()==1 && state.confluxCataclysm$repeated())lifeTicks=290;}
    @Inject(method="onHitEntity",at=@At("TAIL"))
    private void remnant$repeat(EntityHitResult hit,CallbackInfo ci){
        var state=(ManagedProjectile)this;
        if(state.confluxCataclysm$mode()==1 && state.confluxCataclysm$repeated()){
            var storm=(Cursed_Sandstorm_Entity)(Object)this;storm.setHit(2);storm.setState(0);
        }
    }
}
