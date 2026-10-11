package dev.cataclysmnouveau.mixin;
import com.github.L_Ender.cataclysm.entity.projectile.Lava_Bomb_Entity;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(value=Lava_Bomb_Entity.class,remap=false)
public abstract class LavaBombMixin {
    @WrapOperation(method={"onHitEntity","onHitBlock"},at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;DDDFLnet/minecraft/world/level/Level$ExplosionInteraction;)Lnet/minecraft/world/level/Explosion;"))
    private net.minecraft.world.level.Explosion confluxCataclysm$explosion(Level level,Entity source,double x,double y,double z,float power,Level.ExplosionInteraction interaction,Operation<net.minecraft.world.level.Explosion> original) {
        var bomb=(Lava_Bomb_Entity)(Object)this;
        return ((ManagedProjectile)bomb).confluxCataclysm$mode()>0?LavaCasting.explosion(bomb,level,x,y,z,power):original.call(level,source,x,y,z,power,interaction);
    }
    @WrapOperation(method="doTerrainEffects",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;setBlockAndUpdate(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean confluxCataclysm$temporarySource(Level level,BlockPos pos,BlockState block,Operation<Boolean> original) {
        var bomb=(Lava_Bomb_Entity)(Object)this;
        if(((ManagedProjectile)bomb).confluxCataclysm$mode()==0) return original.call(level,pos,block);
        if(!TemporaryLava.claim(bomb,pos)) return false;
        boolean placed=original.call(level,pos,block);if(!placed) TemporaryLava.release(bomb);return placed;
    }
    @Inject(method="remove",at=@At("RETURN"))
    private void confluxCataclysm$release(Entity.RemovalReason reason,CallbackInfo ci) {TemporaryLava.release((Lava_Bomb_Entity)(Object)this);}
    @WrapOperation(method="remove",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;"))
    private net.minecraft.world.level.material.FluidState confluxCataclysm$noChunkLoad(Level level,BlockPos pos,Operation<net.minecraft.world.level.material.FluidState> original) {
        return ((ManagedProjectile)this).confluxCataclysm$mode()>0 && !level.hasChunkAt(pos)
                ?net.minecraft.world.level.material.Fluids.EMPTY.defaultFluidState():original.call(level,pos);
    }
}
