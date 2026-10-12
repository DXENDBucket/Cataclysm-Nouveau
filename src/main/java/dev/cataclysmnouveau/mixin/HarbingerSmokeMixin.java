package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.HarbingerSmoke;
import dev.cataclysmnouveau.casting.HarbingerSmokeParticles;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Wither_Smoke_Effect_Entity.class,remap=false)
public abstract class HarbingerSmokeMixin extends Entity implements HarbingerSmoke {
    @Unique private static final EntityDataAccessor<Boolean> harbinger$MANAGED_SMOKE=SynchedEntityData.defineId(Wither_Smoke_Effect_Entity.class,EntityDataSerializers.BOOLEAN);
    @Unique private float harbinger$amplification;
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void harbinger$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(harbinger$MANAGED_SMOKE,false);}
    public boolean confluxHarbinger$managedSmoke(){return entityData.get(harbinger$MANAGED_SMOKE);}
    public void confluxHarbinger$managedSmoke(boolean value){entityData.set(harbinger$MANAGED_SMOKE,value);}
    @Inject(method="onSyncedDataUpdated",at=@At("TAIL"))
    private void harbinger$particleTicks(EntityDataAccessor<?> key,CallbackInfo ci){
        // Vanilla flag is also respected by Entity Culling; no optional mod class is referenced.
        if(harbinger$MANAGED_SMOKE.equals(key))noCulling=confluxHarbinger$managedSmoke();
    }
    protected HarbingerSmokeMixin(EntityType<?> type,Level level){super(type,level);}
    public float confluxHarbinger$amplification(){return harbinger$amplification;}
    public void confluxHarbinger$amplification(float v){harbinger$amplification=v;}
    @Override public boolean shouldBeSaved(){return !confluxHarbinger$managedSmoke() && NativeSpellDamage.context(this)==null && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void harbinger$alive(CallbackInfo ci) {
        if(level().isClientSide)return;
        var c=NativeSpellDamage.context(this);
        if(c!=null && (c.caster()==null || !c.caster().isAlive() || c.caster().level()!=level())){discard();ci.cancel();}
    }
    @Inject(method="tick",at=@At("RETURN"))
    private void harbinger$broadcastSmoke(CallbackInfo ci){HarbingerSmokeParticles.send((Wither_Smoke_Effect_Entity)(Object)this);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addAlwaysVisibleParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V",remap=true))
    private void harbinger$singleVisualSource(Level world,ParticleOptions particle,double x,double y,double z,double dx,double dy,double dz,Operation<Void> original){
        // Managed smoke arrives through vanilla particle packets, independently of client
        // entity tick culling. Ordinary Cataclysm clouds retain their native emission.
        if(!confluxHarbinger$managedSmoke())original.call(world,particle,x,y,z,dx,dy,dz);
    }
}
