package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Dimensional_Rift_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(value=Dimensional_Rift_Entity.class,remap=false)
public abstract class LeviathanRiftMixin extends Entity implements LeviathanArea {
    @Unique private static final EntityDataAccessor<Integer> leviathan$LIFE=SynchedEntityData.defineId(Dimensional_Rift_Entity.class,EntityDataSerializers.INT);
    protected LeviathanRiftMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void leviathan$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(leviathan$LIFE,0);}
    public int confluxLeviathan$lifetime(){return entityData.get(leviathan$LIFE);}
    public void confluxLeviathan$lifetime(int value){entityData.set(leviathan$LIFE,value);}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){super.onSyncedDataUpdated(key);if(leviathan$LIFE.equals(key) && confluxLeviathan$lifetime()>0)noCulling=true;}
    @Override public boolean shouldBeSaved(){return confluxLeviathan$lifetime()==0 && super.shouldBeSaved();}
    @Inject(method="berserkBlockBreaking",at=@At("HEAD"),cancellable=true)
    private void leviathan$noTerrain(int x,int y,int z,CallbackInfo ci){if(confluxLeviathan$lifetime()>0)ci.cancel();}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",ordinal=0,remap=true))
    private List<Entity> leviathan$pull(Level world,Entity ignored,AABB box,Operation<List<Entity>> original){
        if(confluxLeviathan$lifetime()==0)return original.call(world,ignored,box);
        if(world.isClientSide)return List.of();
        var cast=NativeDamageContexts.context(this);
        return original.call(world,ignored,box).stream().filter(e->{
            if(cast==null || HarbingerCasting.allied(cast,e))return false;
            var other=NativeDamageContexts.context(e);
            return other==null || !dev.arsconflux.api.targeting.Allies.areAllies(cast.caster(),other.caster());
        }).toList();
    }
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;)Ljava/util/List;",ordinal=1,remap=true))
    private List<Entity> leviathan$noForeignDebris(Level world,Entity ignored,AABB box,Operation<List<Entity>> original){return confluxLeviathan$lifetime()>0?List.of():original.call(world,ignored,box);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/Entity;setDeltaMovement(Lnet/minecraft/world/phys/Vec3;)V",remap=true))
    private void leviathan$syncMotion(Entity target,Vec3 motion,Operation<Void> original){original.call(target,motion);if(confluxLeviathan$lifetime()>0)target.hurtMarked=true;}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/The_Leviathan/Dimensional_Rift_Entity;setStage(I)V"))
    private void leviathan$stageAuthority(Dimensional_Rift_Entity rift,int value,Operation<Void> original){if(confluxLeviathan$lifetime()==0 || !level().isClientSide)original.call(rift,value);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/The_Leviathan/Dimensional_Rift_Entity;setLifespan(I)V"))
    private void leviathan$lifeAuthority(Dimensional_Rift_Entity rift,int value,Operation<Void> original){if(confluxLeviathan$lifetime()==0 || !level().isClientSide)original.call(rift,value);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;",remap=true))
    private Explosion leviathan$collapse(Level world,Entity owner,double x,double y,double z,float radius,boolean fire,Level.ExplosionInteraction interaction,Operation<Explosion> original){return confluxLeviathan$lifetime()>0 && !world.isClientSide?LeviathanCasting.explosion(this,world,x,y,z,radius):original.call(world,owner,x,y,z,radius,fire,interaction);}
}
