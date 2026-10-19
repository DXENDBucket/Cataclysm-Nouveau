package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import java.util.function.BooleanSupplier;

public final class LeviathanFormations {
    private static final ThreadLocal<SpellLeviathan> CALL=new ThreadLocal<>();
    public static SpellLeviathan current(){return CALL.get();}
    private static void invoke(SpellLeviathan boss,int age){
        var old=CALL.get();CALL.set(boss);boss.setAnimationTick(age);
        try{boss.nativeGoal().tick();}finally{if(old==null)CALL.remove();else CALL.set(old);}
    }
    public static boolean loaded(double x,double y,double z){
        var boss=current();return boss==null || boss.level().isInWorldBounds(BlockPos.containing(x,y,z)) && boss.level().hasChunkAt(BlockPos.containing(x,y,z));
    }
    public static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp,double duration){
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS || !loadedPoint(world,cast.origin()))return false;
        var frame=cast.withExecutor(null);var boss=new SpellLeviathan(world,frame,ability,cast.origin(),amp,duration);
        LivingEntity selected=cast.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living?living:null;
        var marker=new ArmorStand(world,cast.origin().x,cast.origin().y,cast.origin().z);
        boss.setTarget(selected==null?marker:selected);
        int first=ability==Ability.ABYSS_MINEFIELD?31:56;
        long seed=boss.getRandom().nextLong();boss.getRandom().setSeed(seed);boss.collecting=true;
        try{invoke(boss,first);}finally{boss.collecting=false;}
        if(boss.candidates==0){boss.discard();return false;}
        boss.getRandom().setSeed(seed);
        int max=ability==Ability.ABYSS_MINEFIELD?MonstrosityCasting.scaledTicks(800,duration)+110:MonstrosityCasting.scaledTicks(160,duration)+260;
        return ExecutionSessions.start(new ExecutionSession<>(world,frame,boss,max,new ExecutionDriver<SpellLeviathan>(){
            int age;
            public boolean start(ExecutionSession<SpellLeviathan> s){return true;}
            public boolean tick(ExecutionSession<SpellLeviathan> s){
                if(!loadedPoint(world,boss.position()))return true;
                ++age;
                if(age==first || ability==Ability.ABYSS_BOMBARDMENT_ARRAY && (age==76 || age==96)){
                    if(selected!=null && (!selected.isAlive() || selected.isRemoved() || selected.level()!=world || !world.hasChunkAt(selected.blockPosition())))boss.setTarget(marker);
                    invoke(boss,age);
                }
                for(var child:boss.children)if(!child.isRemoved() && !world.hasChunkAt(child.blockPosition()))remove(child);
                int last=ability==Ability.ABYSS_MINEFIELD?31:96;
                return age>last && boss.children.stream().allMatch(Entity::isRemoved);
            }
            public void close(ExecutionSession<SpellLeviathan> s,ExecutionSession.EndReason reason){boss.clearChildren();marker.discard();boss.discard();}
        },hit->{},()->LeviathanCasting.enabled(ability),ExecutionScope.DIRECT));
    }
    private static boolean loadedPoint(ServerLevel world,Vec3 p){return world.isInWorldBounds(BlockPos.containing(p)) && world.hasChunkAt(BlockPos.containing(p));}
    /** Invoked at the native addFreshEntity call, before any unmanaged entity enters the world. */
    public static boolean spawn(Level world,Entity child,BooleanSupplier original){
        var boss=current();if(boss==null)return original.getAsBoolean();
        if(!world.hasChunkAt(child.blockPosition())){child.discard();return false;}
        if(boss.collecting){boss.candidates++;child.discard();return true;}
        var owner=boss.cast.damageOwner()==null?boss.cast.caster():boss.cast.damageOwner();
        if(child instanceof Abyss_Mine_Entity mine){
            mine.setCaster(owner);((LeviathanArea)mine).confluxLeviathan$lifetime(MonstrosityCasting.scaledTicks(800,boss.duration));
        } else if(child instanceof Abyss_Blast_Portal_Entity portal){
            portal.setCaster(owner);int output=MonstrosityCasting.scaledTicks(160,boss.duration);
            ((LeviathanPortal)portal).confluxLeviathan$beamDuration(output);((LeviathanArea)portal).confluxLeviathan$lifetime(output+100);
        } else {child.discard();return false;}
        NativeDamageContexts.bind(child,boss.cast,boss.amplification);
        boolean spawned=original.getAsBoolean();if(spawned)boss.children.add(child);else child.discard();return spawned;
    }
    public static void remove(Entity child){if(child instanceof LeviathanPortal portal && portal.confluxLeviathan$child()!=null)portal.confluxLeviathan$child().discard();child.discard();}
    private LeviathanFormations(){}
}
