package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.projectile.Tidal_Tentacle_Entity;
import com.github.L_Ender.cataclysm.entity.util.TidalTentacleUtil;
import com.github.L_Ender.cataclysm.init.ModItems;
import com.github.L_Ender.cataclysm.items.Tidal_Claws;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.BooleanSupplier;

/** One native weapon chain, including its attachment gate, links and retraction. */
public final class LeviathanTentacles {
    private static final ThreadLocal<LeviathanTentacles> STARTING=new ThreadLocal<>();
    public final CastContext cast;
    public final double amplification;
    private final Set<Tidal_Tentacle_Entity> segments=new LinkedHashSet<>();
    private boolean cancelled;
    private LeviathanTentacles(CastContext cast,double amp){this.cast=cast;amplification=amp;}
    public static boolean start(ServerLevel world,CastContext cast,double amp){
        if(!(cast.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity target)
                || !target.isAlive() || target.isSpectator() || HarbingerCasting.allied(cast,target)
                || target.level()!=world || cast.caster().distanceToSqr(target)>256 || !world.hasChunkAt(target.blockPosition()))return false;
        if(world.clip(new ClipContext(cast.caster().getEyePosition(),target.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,cast.caster())).getType()!=HitResult.Type.MISS)return false;
        var chain=new LeviathanTentacles(cast,amp);
        return ExecutionSessions.start(new ExecutionSession<>(world,cast.withExecutor(cast.caster()),chain,240,new ExecutionDriver<LeviathanTentacles>(){
            public boolean start(ExecutionSession<LeviathanTentacles> s){
                var old=STARTING.get();STARTING.set(chain);
                try{return ((Tidal_Claws)ModItems.TIDAL_CLAWS.get()).launchTendonsAt(ModItems.TIDAL_CLAWS.get().getDefaultInstance(),cast.caster(),target)
                        && chain.segments.stream().anyMatch(e->!e.isRemoved());}
                finally{if(old==null)STARTING.remove();else STARTING.set(old);}
            }
            public boolean tick(ExecutionSession<LeviathanTentacles> s){
                for(var segment:chain.segments)if(!world.hasChunkAt(segment.blockPosition()))segment.discard();
                return chain.segments.stream().allMatch(Entity::isRemoved);
            }
            public void close(ExecutionSession<LeviathanTentacles> s,ExecutionSession.EndReason reason){chain.close();}
        },h->{},()->LeviathanCasting.enabled(Ability.TIDAL_TENTACLE),ExecutionScope.DIRECT));
    }
    public static boolean initial(Entity entity,BooleanSupplier spawn){
        var chain=STARTING.get();return chain==null?spawn.getAsBoolean():chain.child((Tidal_Tentacle_Entity)entity,spawn);
    }
    public static boolean starting(LivingEntity owner){return STARTING.get()!=null && STARTING.get().cast.caster()==owner;}
    /** Native 3.33's helper only writes hasTentacle; its getter also requires these fields. */
    public static void pointerMetadata(LivingEntity owner,Tidal_Tentacle_Entity segment){
        var attachment=owner.getData(com.github.L_Ender.cataclysm.init.ModDataAttachments.TIDAL_TENTACLE_ATTACHMENT);
        attachment.setLastTentacleUUID(segment==null?null:segment.getUUID());
        attachment.setLastTentacleID(segment==null?-1:segment.getId());
    }
    public boolean child(Tidal_Tentacle_Entity child,BooleanSupplier spawn){
        if(cancelled){child.discard();return false;}
        ((ManagedTidalTentacle)child).confluxLeviathan$chain(this);NativeDamageContexts.bind(child,cast,amplification);segments.add(child);
        boolean result=spawn.getAsBoolean();if(!result)child.discard();return result;
    }
    public boolean owns(Entity entity){return segments.contains(entity);}
    public boolean ownsPointer(){
        var id=cast.caster().getData(com.github.L_Ender.cataclysm.init.ModDataAttachments.TIDAL_TENTACLE_ATTACHMENT).getLastTentacleUUID();
        return id!=null && segments.stream().anyMatch(e->e.getUUID().equals(id));
    }
    private void close(){
        cancelled=true;
        if(ownsPointer()){
            TidalTentacleUtil.setLastTentacle(cast.caster(),null);
            pointerMetadata(cast.caster(),null);
        }
        segments.forEach(Entity::discard);segments.clear();
    }
}
