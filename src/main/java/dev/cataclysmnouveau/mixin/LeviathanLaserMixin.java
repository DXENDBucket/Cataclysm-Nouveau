package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.LeviathanBeam;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=Abyss_Blast_Entity.class,remap=false)
public abstract class LeviathanLaserMixin extends Entity implements LeviathanBeam {
    @Unique private static final EntityDataAccessor<Boolean> leviathan$MANAGED=SynchedEntityData.defineId(Abyss_Blast_Entity.class,EntityDataSerializers.BOOLEAN);
    @Unique private static final EntityDataAccessor<Integer> leviathan$EXECUTOR=SynchedEntityData.defineId(Abyss_Blast_Entity.class,EntityDataSerializers.INT);
    @Unique private boolean leviathan$advance;
    protected LeviathanLaserMixin(EntityType<?> type,Level world){super(type,world);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void leviathan$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(leviathan$MANAGED,false);b.define(leviathan$EXECUTOR,-1);}
    public boolean confluxLeviathan$managed(){return entityData.get(leviathan$MANAGED);}
    public void confluxLeviathan$managed(boolean value){entityData.set(leviathan$MANAGED,value);}
    public void confluxLeviathan$advance(boolean value){leviathan$advance=value;}
    public int confluxLeviathan$executor(){return entityData.get(leviathan$EXECUTOR);}
    public void confluxLeviathan$executor(int id){entityData.set(leviathan$EXECUTOR,id);}
    @Override public boolean shouldBeSaved(){return !confluxLeviathan$managed() && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void leviathan$singleDriver(CallbackInfo ci){if(!level().isClientSide && confluxLeviathan$managed() && !leviathan$advance)ci.cancel();}
    @Inject(method="updateWithHarbinger",at=@At("HEAD"),cancellable=true)
    private void leviathan$pose(CallbackInfo ci){if(confluxLeviathan$managed())ci.cancel();}
    @Inject(method="tick",at=@At(value="FIELD",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/The_Leviathan/Abyss_Blast_Entity;renderPitch:F",opcode=181,shift=At.Shift.AFTER))
    private void leviathan$renderPose(CallbackInfo ci){leviathan$syncPose();}
    @Inject(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/AnimationMonster/BossMonsters/The_Leviathan/Abyss_Blast_Entity;calculateEndPos()V"))
    private void leviathan$untrackedOwner(CallbackInfo ci){leviathan$syncPose();}
    @Unique private void leviathan$syncPose(){if(confluxLeviathan$managed()){var beam=(Abyss_Blast_Entity)(Object)this;beam.renderYaw=beam.getYaw();beam.renderPitch=beam.getPitch();}}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z",remap=true))
    private boolean leviathan$terrain(Level world,BlockPos pos,boolean drops,Operation<Boolean> original){return !confluxLeviathan$managed() && original.call(world,pos,drops);}
}
