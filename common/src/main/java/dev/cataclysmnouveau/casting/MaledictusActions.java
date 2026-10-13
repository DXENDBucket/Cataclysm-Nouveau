package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.*;
import java.util.List;

/** One anchor-based native driver for an executor or a fixed position; only body actions claim an actor. */
final class MaledictusActions {
    static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp) {
        boolean cleave=ability==Ability.HALBERD_CLEAVE;
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS)return false;
        var point=cast.target() instanceof EntityHitResult h?h.getEntity().position():cast.origin();
        var anchor=cleave?ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO)
                :ExecutionAnchor.position(point,MonstrosityCasting.direction(cast));
        if(anchor==null)return false;
        var geometry=new ActionGeometry(world,cast,anchor);int radius=cleave?17:8;
        if(!geometry.present(cast,radius))return false;
        var actor=anchor.entity();var context=cast.withExecutor(actor);
        var boss=new SpellMaledictus(world,context,ability,geometry.point(),geometry.initialFacing,amp);
        var aim=new ArmorStand(world,point.x,point.y,point.z);boss.setTarget(aim);
        var goal=boss.nativeGoal(cleave?26:7);int lifetime=cleave?73:66;
        var nativeDriver=new ExecutionDriver<SpellMaledictus>() {
            int age;boolean started;Vec3 heading=geometry.initialFacing;
            private void pose() {
                if(cleave)geometry.pose(boss,heading);else boss.pose(geometry.point(),heading);
                boss.attackTicks=age;boss.tickCount=age;boss.setDeltaMovement(Vec3.ZERO);
                aim.setPos(geometry.aimPoint(heading));
            }
            public boolean occupiesCaster(){return cleave && actor!=null && actor==cast.caster();}
            public List<ExecutionClaim> claims(ExecutionSession<SpellMaledictus> s){return cleave && actor!=null?List.of(ExecutionClaim.action(actor)):List.of();}
            public Vec3 incomingDirection(){return heading;}
            public boolean start(ExecutionSession<SpellMaledictus> s) {
                started=true;if(cleave){MaledictusCasting.lock(actor);if(actor instanceof Mob mob)mob.getNavigation().stop();}
                heading=geometry.desired(heading);pose();goal.start();pose();return true;
            }
            public boolean tick(ExecutionSession<SpellMaledictus> s) {
                if(!geometry.present(cast,radius)){s.finish(ExecutionSession.EndReason.DENIED);return false;}
                ++age;
                if(cleave)heading=geometry.steer(heading,true);
                pose();goal.tick();pose();boss.aiStep();
                MaledictusVisuals.tick(world,boss,actor,cleave,age);
                return age>=lifetime;
            }
            public void close(ExecutionSession<SpellMaledictus> s,ExecutionSession.EndReason reason) {
                if(reason!=ExecutionSession.EndReason.COMPLETED)boss.cancelChildren();
                if(started)goal.stop();if(cleave)MaledictusCasting.unlock(actor);
                aim.discard();boss.discard();
            }
        };
        var session=new ExecutionSession<>(world,context,boss,lifetime+2,BodyPhantomSync.wrap(ability,boss,nativeDriver),h->{},
                ()->MaledictusCasting.enabled(ability),ExecutionScope.DIRECT);
        return cleave?ExecutionSessions.startReplacing(session):ExecutionSessions.start(session);
    }
    private MaledictusActions() {}
}
