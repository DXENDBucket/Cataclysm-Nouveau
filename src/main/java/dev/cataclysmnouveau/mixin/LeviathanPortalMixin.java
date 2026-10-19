package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Abyss_Blast_Portal_Entity.class,remap=false)
public abstract class LeviathanPortalMixin extends Entity implements LeviathanPortal {
    @Unique private static final EntityDataAccessor<Integer> leviathan$LIFE=SynchedEntityData.defineId(Abyss_Blast_Portal_Entity.class,EntityDataSerializers.INT);
    @Shadow private int lifeTicks;
    @Shadow private int laserdurations;
    @Shadow private boolean clientSideAttackStarted;
    @Unique private Portal_Abyss_Blast_Entity leviathan$child;
    protected LeviathanPortalMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void leviathan$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(leviathan$LIFE,0);}
    public int confluxLeviathan$lifetime(){return entityData.get(leviathan$LIFE);}
    public void confluxLeviathan$lifetime(int ticks){entityData.set(leviathan$LIFE,ticks);}
    public void confluxLeviathan$beamDuration(int ticks){laserdurations=ticks;}
    public Portal_Abyss_Blast_Entity confluxLeviathan$child(){return leviathan$child;}
    public void confluxLeviathan$child(Portal_Abyss_Blast_Entity child){leviathan$child=child;}
    @Override public void onSyncedDataUpdated(EntityDataAccessor<?> key){
        super.onSyncedDataUpdated(key);
        if(leviathan$LIFE.equals(key) && confluxLeviathan$lifetime()>0){
            lifeTicks=confluxLeviathan$lifetime();noCulling=true;
            if(level().isClientSide)clientSideAttackStarted=true;
        }
    }
    @Override public boolean shouldBeSaved(){return confluxLeviathan$lifetime()==0 && super.shouldBeSaved();}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z",remap=true))
    private boolean leviathan$beam(Level world,Entity child,Operation<Boolean> original){
        if(!world.isClientSide && confluxLeviathan$lifetime()>0 && child instanceof Portal_Abyss_Blast_Entity beam)
            LeviathanCasting.bindChild((Abyss_Blast_Portal_Entity)(Object)this,beam);
        return original.call(world,child);
    }
}
