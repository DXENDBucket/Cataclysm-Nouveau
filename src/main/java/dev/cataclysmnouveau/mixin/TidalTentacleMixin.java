package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity;
import com.github.L_Ender.cataclysm.entity.util.TidalTentacleUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

@Mixin(value=Tidal_Tentacle_Entity.class,remap=false)
public abstract class TidalTentacleMixin extends Entity implements ManagedTidalTentacle {
    @Unique private static final EntityDataAccessor<Boolean> leviathan$MANAGED=SynchedEntityData.defineId(Tidal_Tentacle_Entity.class,EntityDataSerializers.BOOLEAN);
    @Unique private LeviathanTentacles leviathan$chain;
    protected TidalTentacleMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void leviathan$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(leviathan$MANAGED,false);}
    public boolean confluxLeviathan$managedTentacle(){return entityData.get(leviathan$MANAGED);}
    public void confluxLeviathan$chain(LeviathanTentacles chain){leviathan$chain=chain;entityData.set(leviathan$MANAGED,true);}
    public LeviathanTentacles confluxLeviathan$chain(){return leviathan$chain;}
    @Override public boolean shouldBeSaved(){return !confluxLeviathan$managedTentacle() && super.shouldBeSaved();}
    @Inject(method="isValidTarget",at=@At("HEAD"),cancellable=true)
    private void leviathan$allies(LivingEntity creator,Entity target,CallbackInfoReturnable<Boolean> ci){
        if(!level().isClientSide && leviathan$chain!=null && HarbingerCasting.allied(leviathan$chain.cast,target))ci.setReturnValue(false);
    }
    @WrapOperation(method="createChain",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean leviathan$child(Level world,Entity child,Operation<Boolean> original){return leviathan$chain==null?original.call(world,child):leviathan$chain.child((Tidal_Tentacle_Entity)child,()->original.call(world,child));}
    @Inject(method="updateLastTendon",at=@At("HEAD"),cancellable=true)
    private void leviathan$attachment(Tidal_Tentacle_Entity next,CallbackInfo ci){
        if(leviathan$chain==null)return;
        if(!leviathan$chain.ownsPointer())ci.cancel();
    }
    @WrapOperation(method="updateLastTendon",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/util/TidalTentacleUtil;setLastTentacle(Lnet/minecraft/world/entity/LivingEntity;Lcom/github/L_Ender/cataclysm/entity/projectile/Tidal_Tentacle_Entity;)V"))
    private void leviathan$metadata(LivingEntity owner,Tidal_Tentacle_Entity segment,Operation<Void> original){
        original.call(owner,segment);
        if(leviathan$chain!=null)LeviathanTentacles.pointerMetadata(owner,segment);
    }
    @ModifyArg(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/damagesource/DamageSources;mobProjectile(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/damagesource/DamageSource;",remap=true),index=1)
    private LivingEntity leviathan$credit(LivingEntity original){return leviathan$chain==null || leviathan$chain.cast.damageOwner()==null?original:leviathan$chain.cast.damageOwner();}
}
