package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Mine_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Abyss_Mine_Entity.class,remap=false)
public abstract class LeviathanMineMixin extends Entity implements LeviathanArea {
    @Unique private static final EntityDataAccessor<Integer> leviathan$LIFE=SynchedEntityData.defineId(Abyss_Mine_Entity.class,EntityDataSerializers.INT);
    @Shadow private int lifeTicks;
    @Shadow private boolean clientSideAttackStarted;
    protected LeviathanMineMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void leviathan$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(leviathan$LIFE,0);}
    public int confluxLeviathan$lifetime(){return entityData.get(leviathan$LIFE);}
    public void confluxLeviathan$lifetime(int ticks){entityData.set(leviathan$LIFE,ticks);}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){
        super.onSyncedDataUpdated(key);
        if(leviathan$LIFE.equals(key) && confluxLeviathan$lifetime()>0){
            lifeTicks=confluxLeviathan$lifetime();noCulling=true;
            if(level().isClientSide)clientSideAttackStarted=true;
        }
    }
    @Override public boolean shouldBeSaved(){return confluxLeviathan$lifetime()==0 && super.shouldBeSaved();}
    @Inject(method="explode",at=@At("HEAD"),cancellable=true)
    private void leviathan$trigger(LivingEntity target,CallbackInfo ci){
        if(level().isClientSide || confluxLeviathan$lifetime()==0)return;
        var cast=NativeDamageContexts.context(this);
        if(isRemoved() || cast!=null && HarbingerCasting.allied(cast,target))ci.cancel();
    }
    @WrapOperation(method="explode",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;",remap=true))
    private Explosion leviathan$explosion(Level world,Entity owner,double x,double y,double z,float radius,Level.ExplosionInteraction interaction,Operation<Explosion> original){
        return !world.isClientSide && confluxLeviathan$lifetime()>0?LeviathanCasting.explosion(this,world,x,y,z,radius):original.call(world,owner,x,y,z,radius,interaction);
    }
}
