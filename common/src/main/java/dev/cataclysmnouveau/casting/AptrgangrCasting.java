package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.projectile.Axe_Blade_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Native state2: one melee smash and the complete five-blade fan at tick15. */
public final class AptrgangrCasting {
    private static final Set<UUID> ACTORS=new HashSet<>();
    public static boolean isActing(LivingEntity actor){return ACTORS.contains(actor.getUUID());}
    public static boolean smash(ServerLevel world,CastContext cast,double amp){
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null)return false;
        var geometry=new ActionGeometry(world,cast,anchor);if(!geometry.present(cast,10))return false;
        var actor=anchor.entity();var context=cast.withExecutor(actor);var frame=new SpellAptrgangr(world,context,anchor,amp);
        var marker=new ArmorStand(world,0,0,0);var goal=frame.smashGoal();
        var driver=new ExecutionDriver<SpellAptrgangr>() {
            int age;boolean started;Vec3 heading=geometry.initialFacing;
            private void pose(){geometry.pose(frame,heading);frame.attackTicks=age;frame.tickCount=age;
                marker.setPos(geometry.aimPoint(heading));frame.setTarget(marker);}
            public boolean occupiesCaster(){return actor==cast.caster();}
            public List<ExecutionClaim> claims(ExecutionSession<SpellAptrgangr> s){return actor==null?List.of():List.of(ExecutionClaim.action(actor));}
            public Vec3 incomingDirection(){return geometry.direction(heading);}
            public boolean start(ExecutionSession<SpellAptrgangr> s){
                started=true;if(actor!=null){ACTORS.add(actor.getUUID());if(actor instanceof Mob mob)mob.getNavigation().stop();}
                heading=geometry.desired(heading);frame.setAttackState(2);pose();goal.start();pose();return true;
            }
            public boolean tick(ExecutionSession<SpellAptrgangr> s){
                if(!geometry.present(cast,10)){s.finish(ExecutionSession.EndReason.DENIED);return false;}
                ++age;heading=geometry.steer(heading,age<10);pose();goal.tick();pose();frame.aiStep();
                return age>=((NativeInternalGoal)goal).confluxMaledictus$duration();
            }
            public void close(ExecutionSession<SpellAptrgangr> s,ExecutionSession.EndReason reason){
                if(reason!=ExecutionSession.EndReason.COMPLETED)for(var child:frame.children)child.finish(reason);
                if(started)goal.stop();if(actor!=null)ACTORS.remove(actor.getUUID());marker.discard();frame.discard();
            }
        };
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world,context,frame,43,
            BodyPhantomSync.wrap(Ability.APTRGANGR_SMASH,frame,driver),h->{},()->GlyphCatalog.APTRGANGR_SMASH.isEnabled(),ExecutionScope.DIRECT));
    }
    public static boolean child(SpellAptrgangr frame,Entity entity,BooleanSupplier original){
        if(!(entity instanceof Axe_Blade_Entity blade) || !(frame.level() instanceof ServerLevel world))return false;
        if(!world.hasChunkAt(blade.blockPosition()))return false;
        blade.setOwner(frame.cast.damageOwner()==null?frame.cast.caster():frame.cast.damageOwner());
        var state=(ManagedProjectile)blade;state.confluxCataclysm$mode(2);state.confluxCataclysm$sourceAbility(Ability.APTRGANGR_SMASH);
        NativeDamageContexts.bind(blade,frame.cast,frame.amplification);
        var child=new ExecutionSession<>(world,frame.cast,blade,83,new ExecutionDriver<Axe_Blade_Entity>() {
            public boolean start(ExecutionSession<Axe_Blade_Entity> s){boolean added=original.getAsBoolean();if(added)s.trackProjectile(blade);return added;}
            public boolean tick(ExecutionSession<Axe_Blade_Entity> s){return blade.isRemoved() || !world.hasChunkAt(blade.blockPosition()) || !world.hasChunkAt(BlockPos.containing(blade.position().add(blade.getDeltaMovement())));}
            public void close(ExecutionSession<Axe_Blade_Entity> s,ExecutionSession.EndReason reason){blade.discard();}
        },h->{},()->GlyphCatalog.APTRGANGR_SMASH.isEnabled() && frame.anchor.valid(world,frame.cast),ExecutionScope.DIRECT);
        if(!ExecutionSessions.start(child))return false;frame.children.add(child);return true;
    }
    private AptrgangrCasting(){}
}
