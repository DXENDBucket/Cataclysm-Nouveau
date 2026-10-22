package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

/** A parallel area effect; no movement, body claim or player-owned replacement animation. */
public final class PriestCasting {
    public static boolean start(ServerLevel world,CastContext cast,double amp){
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null || !anchor.valid(world,cast) || !MonstrosityCasting.loadedArea(world,anchor.sample().origin(),8))return false;
        var context=cast.withExecutor(anchor.entity());var geometry=new ActionGeometry(world,context,anchor);
        var frame=new SpellPriest(world,context,amp);var goal=frame.glareGoal();
        geometry.pose(frame,geometry.initialFacing);
        var driver=new ExecutionDriver<SpellPriest>() {
            int age;boolean started;
            public boolean start(ExecutionSession<SpellPriest> s){
                started=true;goal.start();frame.setAnimationTick(0);return true;
            }
            public boolean tick(ExecutionSession<SpellPriest> s){
                if(!geometry.present(context,8))return true;
                geometry.pose(frame,geometry.desired(geometry.initialFacing));
                frame.tickCount=++age;frame.setAnimationTick(age);frame.tick();
                return age>=57;
            }
            public void close(ExecutionSession<SpellPriest> s,ExecutionSession.EndReason reason){
                if(started)goal.stop();frame.discard();
            }
        };
        return ExecutionSessions.start(new ExecutionSession<>(world,context,frame,60,
            BodyPhantomSync.wrap(Ability.PRIEST_GLARE,frame,driver),h->{},()->RemnantCasting.enabled(Ability.PRIEST_GLARE),ExecutionScope.DIRECT));
    }
    private PriestCasting(){}
}
