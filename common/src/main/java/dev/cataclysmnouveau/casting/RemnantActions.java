package dev.cataclysmnouveau.casting;

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
import java.util.*;

/** Native goals own impulses and attacks; the same driver serves actors and detached positions. */
public final class RemnantActions {
    private static final Set<UUID> ACTORS=new HashSet<>();
    public static boolean isActing(LivingEntity actor){return ACTORS.contains(actor.getUUID());}
    static boolean supports(Ability a){return a==Ability.REMNANT_CHARGE || a==Ability.REMNANT_TAIL_SWEEP;}
    static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null)return false;
        var geometry=new ActionGeometry(world,cast,anchor);
        if(!geometry.present(cast,12))return false;
        var actor=anchor.entity();var context=cast.withExecutor(actor);
        var frame=new SpellRemnant(world,context,ability,geometry.point(),geometry.initialFacing,amp);
        var marker=new ArmorStand(world,0,0,0);boolean charge=ability==Ability.REMNANT_CHARGE;
        var driver=new ExecutionDriver<SpellRemnant>() {
            int phaseAge;boolean started;Goal goal;
            Vec3 heading=geometry.initialFacing,velocity=Vec3.ZERO;
            private void pose() {
                if(actor!=null)geometry.pose(frame,heading);else {
                    frame.pose(frame.position(),heading);var p=frame.position();
                    frame.setBoundingBox(new AABB(p.x-.3,p.y,p.z-.3,p.x+.3,p.y+1.8,p.z+.3));
                }
                frame.attackTicks=phaseAge;frame.tickCount=phaseAge;
                if(actor!=null){velocity=actor.getDeltaMovement();frame.setOnGround(actor.onGround());frame.horizontalCollision=actor.horizontalCollision;}
                frame.setDeltaMovement(velocity);
                var point=actor==null?frame.position().add(heading.scale(6)):geometry.aimPoint(heading);
                marker.setPos(point);frame.setTarget(marker);
            }
            private void begin(int state){
                phaseAge=0;frame.setAttackState(state);goal=frame.nativeGoal(state);pose();goal.start();pose();
            }
            public boolean occupiesCaster(){return actor==cast.caster();}
            public List<ExecutionClaim> claims(ExecutionSession<SpellRemnant> s){return actor==null?List.of():List.of(ExecutionClaim.action(actor));}
            public Vec3 incomingDirection(){return geometry.direction(heading);}
            public boolean start(ExecutionSession<SpellRemnant> s){
                started=true;if(actor!=null){ACTORS.add(actor.getUUID());if(actor instanceof Mob mob)mob.getNavigation().stop();}
                heading=geometry.desired(heading);
                if(actor==null){frame.setBoundingBox(geometry.bounds());frame.setOnGround(!world.noCollision(frame,frame.getBoundingBox().move(0,-.05,0)));}
                begin(charge?10:16);return true;
            }
            public boolean tick(ExecutionSession<SpellRemnant> s){
                if(!geometry.present(cast,12) || !MonstrosityCasting.loadedArea(world,frame.position(),12)){
                    s.finish(ExecutionSession.EndReason.DENIED);return false;
                }
                int state=frame.getAttackState();
                if(actor==null && charge){
                    var next=frame.position().add(velocity);
                    if(!world.hasChunkAt(BlockPos.containing(next))){s.finish(ExecutionSession.EndReason.WORLD_UNLOADED);return false;}
                    frame.move(MoverType.SELF,velocity);
                    velocity=new Vec3(frame.horizontalCollision?0:velocity.x,frame.verticalCollision?0:velocity.y,frame.horizontalCollision?0:velocity.z);
                    velocity=velocity.add(0,-.08,0).multiply(frame.onGround()?.6:.91,.98,frame.onGround()?.6:.91);
                }
                if(charge && state==11 && (actor==null?frame.horizontalCollision:actor.horizontalCollision))return true;
                ++phaseAge;heading=geometry.steer(heading,state==11 || state==10 && phaseAge<66 || state==16 && phaseAge<13);pose();
                var input=velocity;goal.tick();var motion=frame.getDeltaMovement();
                // Native goals may rotate the helper; damage, motion and visuals share the executor's aim.
                pose();frame.setDeltaMovement(motion);
                var collisionBox=frame.getBoundingBox();
                try {
                    // Charge contacts use the native Boss footprint; collision still uses the actual actor.
                    if(charge && state==11)frame.setBoundingBox(frame.getDimensions(frame.getPose()).makeBoundingBox(frame.position()));
                    RemnantCasting.invoke(e->RemnantCasting.add(world,frame,e,amp),frame::aiStep);
                } finally {frame.setBoundingBox(collisionBox);}
                if(charge && state==11 && phaseAge%8==1 && motion.horizontalDistanceSqr()>1e-6)
                    world.playSound(null,frame.getX(),frame.getY(),frame.getZ(),com.github.L_Ender.cataclysm.init.ModSounds.REMNANT_CHARGE_STEP.get(),net.minecraft.sounds.SoundSource.HOSTILE,1F,1F);
                if(actor==null)velocity=motion;
                else if(!motion.equals(input)){actor.setDeltaMovement(motion);actor.hasImpulse=true;actor.hurtMarked=true;}
                if(phaseAge>=((NativeInternalGoal)goal).confluxMaledictus$duration()){
                    goal.stop();goal=null;
                    if(charge && state==10){begin(11);return false;}
                    return true;
                }
                return false;
            }
            public void close(ExecutionSession<SpellRemnant> s,ExecutionSession.EndReason reason){
                if(goal!=null)goal.stop();
                if(started && actor!=null){ACTORS.remove(actor.getUUID());if(charge && !actor.isRemoved()){
                    actor.setDeltaMovement(actor.getDeltaMovement().multiply(.2,1,.2));actor.hurtMarked=true;
                }}
                marker.discard();frame.clear();
            }
        };
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world,context,frame,132,
            BodyPhantomSync.wrap(ability,frame,driver),h->{},()->RemnantCasting.enabled(ability),ExecutionScope.DIRECT));
    }
    private RemnantActions(){}
}
