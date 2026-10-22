package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.*;
import java.util.*;

/** One native action driver for self, ally and detached-position execution. */
public final class BerserkerCasting {
    private static final Set<UUID> ACTORS=new HashSet<>();
    public static boolean isActing(LivingEntity actor){return ACTORS.contains(actor.getUUID());}
    public static boolean supports(Ability a){return a==Ability.BERSERKER_CROSS_SLASH || a==Ability.BERSERKER_SWORD_DANCE || a==Ability.BERSERKER_SPIN;}
    public static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp){
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);if(anchor==null)return false;
        var geometry=new ActionGeometry(world,cast,anchor);if(!geometry.present(cast,8))return false;
        var actor=anchor.entity();var context=cast.withExecutor(actor);var frame=new SpellBerserker(world,context,amp);
        var marker=new ArmorStand(world,0,0,0);boolean dance=ability==Ability.BERSERKER_SWORD_DANCE;
        int first=ability==Ability.BERSERKER_CROSS_SLASH?1:dance?(world.random.nextBoolean()?5:6):2;
        var driver=new ExecutionDriver<SpellBerserker>() {
            int phaseAge,totalAge;boolean started;Goal goal;Vec3 heading=geometry.initialFacing,velocity=Vec3.ZERO;
            private void pose(){
                if(actor!=null)geometry.pose(frame,heading);else{
                    frame.setYRot((float)Math.toDegrees(Math.atan2(-heading.x,heading.z)));frame.yRotO=frame.yBodyRot=frame.yHeadRot=frame.getYRot();
                    var p=frame.position();frame.setBoundingBox(new AABB(p.x-.3,p.y,p.z-.3,p.x+.3,p.y+1.8,p.z+.3));
                }
                frame.attackTicks=phaseAge;frame.tickCount=totalAge;
                if(actor!=null){velocity=actor.getDeltaMovement();frame.setOnGround(actor.onGround());}
                frame.setDeltaMovement(velocity);marker.setPos(actor==null?frame.position().add(heading.scale(6)):geometry.aimPoint(heading));frame.setTarget(marker);
            }
            private void begin(int state){phaseAge=0;frame.setAttackState(state);goal=frame.nativeGoal(state);pose();goal.start();pose();}
            public boolean occupiesCaster(){return actor==cast.caster();}
            public List<ExecutionClaim> claims(ExecutionSession<SpellBerserker> s){return actor==null?List.of():List.of(ExecutionClaim.action(actor));}
            public Vec3 incomingDirection(){return geometry.direction(heading);}
            public boolean start(ExecutionSession<SpellBerserker> s){
                started=true;if(actor!=null){ACTORS.add(actor.getUUID());if(actor instanceof Mob m)m.getNavigation().stop();}
                frame.setPos(geometry.point());heading=geometry.desired(heading);
                if(actor==null){frame.setBoundingBox(geometry.bounds());frame.setOnGround(!world.noCollision(frame,frame.getBoundingBox().move(0,-.05,0)));}
                begin(first);return true;
            }
            public boolean tick(ExecutionSession<SpellBerserker> s){
                if(!geometry.present(cast,8) || !MonstrosityCasting.loadedArea(world,frame.position(),8)){s.finish(ExecutionSession.EndReason.DENIED);return false;}
                if(actor==null && dance){
                    if(!world.hasChunkAt(BlockPos.containing(frame.position().add(velocity)))){s.finish(ExecutionSession.EndReason.WORLD_UNLOADED);return false;}
                    frame.move(MoverType.SELF,velocity);
                    velocity=new Vec3(frame.horizontalCollision?0:velocity.x,frame.verticalCollision?0:velocity.y,frame.horizontalCollision?0:velocity.z)
                        .add(0,-.08,0).multiply(frame.onGround()?.6:.91,.98,frame.onGround()?.6:.91);
                }
                ++phaseAge;++totalAge;int state=frame.getAttackState();
                heading=geometry.steer(heading,dance || state==1 && phaseAge<15 || state==2 && phaseAge<25);pose();
                var input=velocity;goal.tick();var motion=frame.getDeltaMovement();pose();frame.setDeltaMovement(motion);
                var collisionBox=frame.getBoundingBox();
                try{frame.setBoundingBox(frame.getDimensions(frame.getPose()).makeBoundingBox(frame.position()));frame.aiStep();}
                finally{frame.setBoundingBox(collisionBox);}
                motion=frame.getDeltaMovement();
                if(actor==null)velocity=motion;
                else if(!motion.equals(input)){actor.setDeltaMovement(motion);actor.hasImpulse=true;actor.hurtMarked=true;}
                if(phaseAge>=((NativeInternalGoal)goal).confluxMaledictus$duration()){
                    goal.stop();goal=null;if(state==2){begin(3);return false;}if(state==3){begin(4);return false;}return true;
                }
                return false;
            }
            public void close(ExecutionSession<SpellBerserker> s,ExecutionSession.EndReason reason){
                if(goal!=null)goal.stop();if(started && actor!=null)ACTORS.remove(actor.getUUID());marker.discard();frame.discard();
            }
        };
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world,context,frame,163,
            BodyPhantomSync.wrap(ability,frame,driver),h->{},()->enabled(ability),ExecutionScope.DIRECT));
    }
    private static boolean enabled(Ability a){return switch(a){case BERSERKER_CROSS_SLASH->GlyphCatalog.BERSERKER_CROSS_SLASH.isEnabled();case BERSERKER_SWORD_DANCE->GlyphCatalog.BERSERKER_SWORD_DANCE.isEnabled();case BERSERKER_SPIN->GlyphCatalog.BERSERKER_SPIN.isEnabled();default->false;};}
    private BerserkerCasting(){}
}
