package dev.cataclysmnouveau.casting;

import net.minecraft.core.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.saveddata.SavedData;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

/** Persist cleanup positions, not the spell. Reloaded/unloaded pools never become permanent sources. */
@EventBusSubscriber(modid="cataclysm_nouveau")
public final class TemporaryLavaData extends SavedData {
    private static final String ID="cataclysm_nouveau_temporary_lava";
    private static final Factory<TemporaryLavaData> FACTORY=new Factory<>(TemporaryLavaData::new,TemporaryLavaData::load,null);
    final Map<BlockPos,UUID> sources=new HashMap<>();
    final Set<UUID> active=new HashSet<>();
    public static TemporaryLavaData get(ServerLevel world,boolean create) {
        return create?world.getDataStorage().computeIfAbsent(FACTORY,ID):world.getDataStorage().get(FACTORY,ID);
    }
    public static TemporaryLavaData load(CompoundTag tag,HolderLookup.Provider lookup) {
        var data=new TemporaryLavaData();
        for(var entry:tag.getList("sources",Tag.TAG_COMPOUND)) {
            var row=(CompoundTag)entry;
            if(row.hasUUID("bomb")) data.sources.put(BlockPos.of(row.getLong("pos")),row.getUUID("bomb"));
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag tag,HolderLookup.Provider lookup) {
        var list=new ListTag();sources.forEach((pos,id)->{var row=new CompoundTag();row.putLong("pos",pos.asLong());row.putUUID("bomb",id);list.add(row);});
        tag.put("sources",list);return tag;
    }
    @SubscribeEvent public static void tick(ServerTickEvent.Post event) {
        if(event.getServer().getTickCount()%20!=0) return;
        for(var world:event.getServer().getAllLevels()) cleanup(world);
    }
    public static void cleanup(ServerLevel world) {
        var data=get(world,false);if(data==null) return;
        // Block updates also invalidate source records through the Level hook.
        for(var row:List.copyOf(data.sources.entrySet())) {
            if(data.active.contains(row.getValue()) || !world.hasChunkAt(row.getKey())) continue;
            if(world.getBlockState(row.getKey()).is(Blocks.LAVA)) world.setBlockAndUpdate(row.getKey(),Blocks.AIR.defaultBlockState());
            data.sources.remove(row.getKey(),row.getValue());data.setDirty();
        }
    }
}
