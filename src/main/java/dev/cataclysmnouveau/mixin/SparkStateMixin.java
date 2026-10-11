package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Spark_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.casting.ScyllaPayload;
import net.minecraft.network.syncher.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Spark_Entity.class,remap=false)
public abstract class SparkStateMixin implements ScyllaPayload {
    @Unique private static final EntityDataAccessor<Integer> confluxScylla$MODE=SynchedEntityData.defineId(Spark_Entity.class,EntityDataSerializers.INT);
    @Unique private CastContext confluxScylla$cast;
    @Unique private Level confluxScylla$ownerWorld;
    public CastContext confluxScylla$cast() {return confluxScylla$cast;}
    public void confluxScylla$cast(CastContext cast) {confluxScylla$cast=cast;confluxScylla$ownerWorld=cast==null?null:cast.caster().level();}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void confluxScylla$define(SynchedEntityData.Builder builder,CallbackInfo ci) {builder.define(confluxScylla$MODE,0);}
    public int confluxCataclysm$mode() {return ((Spark_Entity)(Object)this).getEntityData().get(confluxScylla$MODE);}
    public void confluxCataclysm$mode(int mode) {((Spark_Entity)(Object)this).getEntityData().set(confluxScylla$MODE,mode);}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void confluxScylla$cleanup(CallbackInfo ci) {
        var spark=(Spark_Entity)(Object)this;
        if(confluxScylla$cast==null || spark.level().isClientSide) return;
        var owner=confluxScylla$cast.caster();
        if(!owner.isAlive() || owner.isRemoved() || owner.level()!=confluxScylla$ownerWorld || spark.tickCount>1200) {spark.discard();ci.cancel();}
    }
}
