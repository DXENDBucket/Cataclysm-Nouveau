package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Death_Laser_Beam_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.CataclysmConfig;
import dev.cataclysmnouveau.casting.ManagedDeathLaser;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Death_Laser_Beam_Entity.class,remap=false)
public abstract class DeathLaserMixin extends Entity implements ManagedDeathLaser {
    @Unique private static final EntityDataAccessor<Boolean> harbinger$MANAGED=SynchedEntityData.defineId(Death_Laser_Beam_Entity.class,EntityDataSerializers.BOOLEAN);
    @Unique private boolean harbinger$advance;
    protected DeathLaserMixin(EntityType<?> type,Level level){super(type,level);}
    @Inject(method="defineSynchedData",at=@At("TAIL"))
    private void harbinger$define(SynchedEntityData.Builder b,CallbackInfo ci){b.define(harbinger$MANAGED,false);}
    public boolean confluxHarbinger$managed(){return entityData.get(harbinger$MANAGED);}
    public void confluxHarbinger$managed(boolean v){entityData.set(harbinger$MANAGED,v);}
    public void confluxHarbinger$advance(boolean v){harbinger$advance=v;}
    @Override public boolean shouldBeSaved(){return !confluxHarbinger$managed() && super.shouldBeSaved();}
    @Inject(method="tick",at=@At("HEAD"),cancellable=true)
    private void harbinger$ownedTick(CallbackInfo ci){if(!level().isClientSide && confluxHarbinger$managed() && !harbinger$advance)ci.cancel();}
    @Inject(method={"updateWithHarbinger","updateWithProwler"},at=@At("HEAD"),cancellable=true)
    private void harbinger$pose(CallbackInfo ci){if(confluxHarbinger$managed())ci.cancel();}
    @Inject(method="tick",at=@At(value="FIELD",target="Lcom/github/L_Ender/cataclysm/entity/projectile/Death_Laser_Beam_Entity;renderPitch:F",opcode=181,shift=At.Shift.AFTER))
    private void harbinger$facing(CallbackInfo ci){if(confluxHarbinger$managed()){var beam=(Death_Laser_Beam_Entity)(Object)this;beam.renderYaw=beam.getYaw();beam.renderPitch=beam.getPitch();}}
    // The damage owner may be outside the client's entity tracking range. Synced pose remains usable.
    @Inject(method="tick",at=@At(value="INVOKE",target="Lcom/github/L_Ender/cataclysm/entity/projectile/Death_Laser_Beam_Entity;calculateEndPos()V"))
    private void harbinger$untrackedOwner(CallbackInfo ci){if(confluxHarbinger$managed()){var beam=(Death_Laser_Beam_Entity)(Object)this;beam.renderYaw=beam.getYaw();beam.renderPitch=beam.getPitch();}}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z",remap=true))
    private boolean harbinger$glass(Level world,BlockPos pos,boolean drops,Operation<Boolean> original){return !confluxHarbinger$managed() && original.call(world,pos,drops);}
    @WrapOperation(method="tick",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z",remap=true))
    private boolean harbinger$blocks(Level world,BlockPos pos,BlockState state,Operation<Boolean> original){return (!confluxHarbinger$managed() || CataclysmConfig.harbingerTerrainFire() && state.getBlock() instanceof BaseFireBlock) && original.call(world,pos,state);}
}
