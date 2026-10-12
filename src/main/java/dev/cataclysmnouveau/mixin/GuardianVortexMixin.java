package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(value=Void_Vortex_Entity.class,remap=false)
public abstract class GuardianVortexMixin extends Entity implements GuardianChild {
    @Unique private static final EntityDataAccessor<Boolean> guardian$MANAGED=SynchedEntityData.defineId(Void_Vortex_Entity.class,EntityDataSerializers.BOOLEAN);
    protected GuardianVortexMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void guardian$define(SynchedEntityData.Builder builder,CallbackInfo ci){builder.define(guardian$MANAGED,false);}
    public boolean confluxGuardian$managed(){return entityData.get(guardian$MANAGED);}
    public void confluxGuardian$managed(boolean value){entityData.set(guardian$MANAGED,value);}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        // This must run on the receiving entity too, not only where the server spawns it.
        if(guardian$MANAGED.equals(key))noCulling=confluxGuardian$managed();
    }
    @Override public AABB getBoundingBoxForCulling() {
        // Native rendering covers a 6x6 ground plane and a descending trail above it.
        // Keep the physical box unchanged; this box describes only the visual extent.
        return confluxGuardian$managed()?new AABB(getX()-3,getY(),getZ()-3,getX()+3,getY()+6,getZ()+3):super.getBoundingBoxForCulling();
    }
    @Override public boolean shouldBeSaved(){return !confluxGuardian$managed() && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void guardian$rootAlive(CallbackInfo ci) {
        // Root lifetime is server-authoritative. Integrated-server entity IDs are shared
        // by their client replicas; a server attachment must never discard the replica.
        if(level().isClientSide)return;
        var cast=NativeSpellDamage.context(this);
        if(confluxGuardian$managed() && cast!=null && (cast.caster()==null || !cast.caster().isAlive() || cast.caster().level()!=level())){discard();ci.cancel();}
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getEntitiesOfClass(Ljava/lang/Class;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",remap=true))
    private List<LivingEntity> guardian$pullTargets(Level world,Class<LivingEntity> type,AABB box,Operation<List<LivingEntity>> original) {
        if(!confluxGuardian$managed())return original.call(world,type,box);
        // Keep native client visuals, but motion is authoritative on the server.
        if(world.isClientSide)return List.of();
        var cast=NativeSpellDamage.context(this);
        return original.call(world,type,box).stream().filter(e->cast==null || !HarbingerCasting.allied(cast,e)).toList();
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/LivingEntity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",remap=true))
    private void guardian$syncPull(LivingEntity entity,Vec3 motion,Operation<Void> original){original.call(entity,motion);if(confluxGuardian$managed())entity.hurtMarked=true;}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;",remap=true))
    private Explosion guardian$ending(Level world,Entity source,double x,double y,double z,float radius,boolean fire,Level.ExplosionInteraction interaction,Operation<Explosion> original) {
        if(!confluxGuardian$managed())return original.call(world,source,x,y,z,radius,fire,interaction);
        return world.isClientSide?null:GuardianCasting.explosion(this,world,x,y,z,radius);
    }
}
