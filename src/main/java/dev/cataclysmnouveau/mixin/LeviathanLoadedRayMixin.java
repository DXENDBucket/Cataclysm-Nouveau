package dev.cataclysmnouveau.mixin;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import com.llamalad7.mixinextras.injector.wrapoperation.*;
import dev.cataclysmnouveau.casting.LeviathanBeam;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/** Preserve nearby output without querying blocks in an unloaded continuation of the ray. */
@Mixin(value={Abyss_Blast_Entity.class,Portal_Abyss_Blast_Entity.class},remap=false)
public abstract class LeviathanLoadedRayMixin {
    @WrapOperation(method="raytraceEntities",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/Level;clip(Lnet/minecraft/world/level/ClipContext;)Lnet/minecraft/world/phys/BlockHitResult;",remap=true))
    private BlockHitResult leviathan$loadedRay(Level world,ClipContext clip,Operation<BlockHitResult> original){
        if(!((LeviathanBeam)this).confluxLeviathan$managed())return original.call(world,clip);
        Vec3 from=clip.getFrom(),end=clip.getTo(),delta=end.subtract(from),last=from;
        int steps=Math.max(1,(int)Math.ceil(delta.length()*2));
        for(int i=1;i<=steps;i++){
            Vec3 point=from.add(delta.scale(i/(double)steps));
            if(!world.hasChunkAt(BlockPos.containing(point)))return original.call(world,new ClipContext(from,last,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,(Entity)(Object)this));
            last=point;
        }
        return original.call(world,clip);
    }
}
