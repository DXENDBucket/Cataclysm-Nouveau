package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.projectile.Lava_Bomb_Entity;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.event.EventHooks;
import java.util.*;

/** Only sources made by opt-in bomb instances are non-spreading. Native lava is untouched. */
public final class TemporaryLava {
    public static boolean mayChange(LivingEntity owner,BlockPos pos) {
        if(owner==null || !(owner.level() instanceof ServerLevel level) || !level.hasChunkAt(pos) || !level.isInWorldBounds(pos)) return false;
        if(owner instanceof Player player && !level.mayInteract(player,pos)) return false;
        return EventHooks.canEntityGrief(level,owner) && EventHooks.onEntityDestroyBlock(owner,pos,level.getBlockState(pos));
    }
    public static boolean claim(Lava_Bomb_Entity bomb,BlockPos pos) {
        if(!(bomb.getOwner() instanceof LivingEntity owner) || !mayChange(owner,pos) || !bomb.level().getBlockState(pos).isAir()) return false;
        for(var direction:Direction.values()) if(bomb.level().getFluidState(pos.relative(direction)).is(FluidTags.WATER)) return false;
        var data=TemporaryLavaData.get((ServerLevel)bomb.level(),true);
        data.sources.put(pos.immutable(),bomb.getUUID());data.active.add(bomb.getUUID());data.setDirty();return true;
    }
    public static boolean contains(Level level,BlockPos pos) {
        if(!(level instanceof ServerLevel world)) return false;
        var data=TemporaryLavaData.get(world,false);return data!=null && data.sources.containsKey(pos);
    }
    public static void release(Lava_Bomb_Entity bomb) {
        if(!(bomb.level() instanceof ServerLevel world)) return;
        var data=TemporaryLavaData.get(world,false);if(data==null) return;
        data.active.remove(bomb.getUUID());
        data.sources.entrySet().removeIf(e->e.getValue().equals(bomb.getUUID()) && world.hasChunkAt(e.getKey()));
        data.setDirty();
    }
    public static void consumed(Level level,BlockPos pos) {
        if(!(level instanceof ServerLevel world)) return;
        var data=TemporaryLavaData.get(world,false);if(data==null) return;
        var id=data.sources.remove(pos);if(id==null) return;
        data.active.remove(id);data.setDirty();
        if(world.getEntity(id) instanceof Lava_Bomb_Entity bomb) bomb.setLavaPos(BlockPos.ZERO);
    }
    public static int count(Level level) {if(!(level instanceof ServerLevel world)) return 0;var data=TemporaryLavaData.get(world,false);return data==null?0:data.sources.size();}
    private TemporaryLava() {}
}
