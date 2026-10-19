package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.*;

/** Complete native formations. A parent session owns every child and its single group quote. */
public final class RemnantFormations {
    public static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp) {
        boolean array=ability==Ability.DESERT_STELE_ARRAY;
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),24))return false;
        if(!array && NativePayloads.groundPoint(world,cast)==null)return false;
        var geometry=new ActionGeometry(world,cast,anchor);
        var context=cast.withExecutor(anchor.entity());var pose=anchor.sample();
        var frame=new SpellRemnant(world,context,ability,pose.origin(),pose.direction(),amp);
        Goal goal=array?frame.monolithGoal():null;var marker=new ArmorStand(world,0,0,0);
        var driver=new ExecutionDriver<SpellRemnant>() {
            int age;boolean started;Vec3 heading=geometry.initialFacing;
            private void pose(){
                var sample=anchor.sample();heading=geometry.steer(heading,array?age<20:age<26 || age>26 && age<55 || age>55 && age<85);geometry.pose(frame,heading);
                // The line aims along the same actor/fixed heading; the target's height is the floor level.
                var aim=sample.origin().add(heading.scale(12));
                marker.setPos(aim.x,sample.origin().y,aim.z);var enemy=geometry.projectileTarget(geometry.direction(heading));frame.setTarget(enemy==null?marker:enemy);frame.attackTicks=age;
            }
            public boolean start(ExecutionSession<SpellRemnant> s){
                started=true;frame.setAttackState(array?17:15);pose();if(goal!=null){goal.start();pose();}return true;
            }
            private boolean birth(Entity child){return RemnantCasting.add(world,frame,child,amp);}
            public boolean tick(ExecutionSession<SpellRemnant> s){
                if(!anchor.valid(world,context) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),24))return true;
                ++age;pose();
                if(array && age<=70){RemnantCasting.invoke(this::birth,goal::tick);geometry.pose(frame,heading);}
                if(age<=(array?70:110))RemnantCasting.invoke(this::birth,frame::aiStep);
                for(var child:frame.children)if(!child.isRemoved() && (!world.hasChunkAt(child.blockPosition())
                    || !world.hasChunkAt(BlockPos.containing(child.position().add(child.getDeltaMovement())))))child.discard();
                return age>(array?70:110) && frame.children.stream().allMatch(Entity::isRemoved) || age>200;
            }
            public void close(ExecutionSession<SpellRemnant> s,ExecutionSession.EndReason reason){if(started && goal!=null)goal.stop();marker.discard();frame.clear();}
        };
        return ExecutionSessions.start(new ExecutionSession<>(world,context,frame,205,
            BodyPhantomSync.wrap(ability,frame,driver),hit->{},()->RemnantCasting.enabled(ability),ExecutionScope.DIRECT));
    }
    private RemnantFormations(){}
}
