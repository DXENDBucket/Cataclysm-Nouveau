package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.Ability;
import dev.cataclysmnouveau.mixin.*;
import dev.cataclysmnouveau.network.BodyPhantomSync;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.*;
import java.util.List;

/** Native phase transitions and attack bodies; a single driver owns both physical and position execution. */
final class MaledictusCombatActions {
    static boolean supports(Ability a){return initialState(a)!=0;}
    private static int initialState(Ability a){return switch(a){
        case MALEDICTUS_AERIAL_SMASH->24;case PHANTOM_AERIAL_VOLLEY->3;case MALEDICTUS_RUSH->10;
        case MALEDICTUS_SHOCKWAVE->9;case MALEDICTUS_UPPERCUT->22;case MALEDICTUS_MELEE_COMBO->19;case MALEDICTUS_SPIN_SLASHES->18;default->0;
    };}
    static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp) {
        if(!supports(ability) || cast.target()==null || cast.target().getType()==HitResult.Type.MISS)return false;
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null)return false;
        var geometry=new ActionGeometry(world,cast,anchor);
        if(!geometry.present(cast,18))return false;
        var actor=anchor.entity();var context=cast.withExecutor(actor);
        var boss=new SpellMaledictus(world,context,ability,geometry.point(),geometry.initialFacing,amp);
        var marker=new ArmorStand(world,0,0,0);
        boolean aerial=ability==Ability.MALEDICTUS_AERIAL_SMASH || ability==Ability.PHANTOM_AERIAL_VOLLEY;
        var driver=new ExecutionDriver<SpellMaledictus>() {
            int age,phaseAge;boolean started,hadNoGravity;Goal goal;
            Vec3 heading=geometry.initialFacing,velocity=Vec3.ZERO;
            private void pose() {
                if(actor!=null)geometry.pose(boss,heading);else fixedPose();
                boss.attackTicks=phaseAge;boss.tickCount=phaseAge;
                if(actor!=null){boss.setOnGround(actor.onGround());boss.verticalCollisionBelow=actor.verticalCollisionBelow;velocity=actor.getDeltaMovement();}
                boss.setDeltaMovement(velocity);
                var enemy=geometry.projectileTarget(geometry.direction(heading));
                var point=geometry.aimPoint(heading);
                if(actor==null){point=boss.position().add(heading.scale(6));point=new Vec3(point.x,geometry.point().y,point.z);}
                marker.setPos(point);boss.setTarget(enemy==null?marker:enemy);
            }
            private void fixedPose() {
                boss.pose(boss.position(),heading);var p=boss.position();
                boss.setBoundingBox(new AABB(p.x-.3,p.y,p.z-.3,p.x+.3,p.y+1.8,p.z+.3));
            }
            private void begin(int state) {
                goal=boss.nativeGoal(state);phaseAge=0;boss.setAttackState(state);pose();goal.start();velocity=boss.getDeltaMovement();
                if(actor!=null){actor.setDeltaMovement(velocity);actor.hasImpulse=true;actor.hurtMarked=true;}pose();
            }
            private boolean applyMotion(ExecutionSession<SpellMaledictus> s,Vec3 input) {
                var motion=boss.getDeltaMovement();
                if(!world.hasChunkAt(BlockPos.containing(boss.position().add(motion)))){s.finish(ExecutionSession.EndReason.WORLD_UNLOADED);return false;}
                if(actor!=null) {
                    if(!motion.equals(input)){actor.setDeltaMovement(motion);actor.hasImpulse=true;actor.hurtMarked=true;}
                    if(aerial){actor.setNoGravity(boss.isFlying());actor.fallDistance=0;}
                } else velocity=motion;
                return true;
            }
            public boolean occupiesCaster(){return actor==cast.caster();}
            public List<ExecutionClaim> claims(ExecutionSession<SpellMaledictus> s){return actor==null?List.of():List.of(ExecutionClaim.action(actor));}
            public Vec3 incomingDirection(){return geometry.direction(heading);}
            public boolean start(ExecutionSession<SpellMaledictus> s) {
                started=true;MaledictusCasting.lock(actor);
                if(actor!=null){hadNoGravity=actor.isNoGravity();if(actor instanceof Mob mob)mob.getNavigation().stop();}
                heading=geometry.desired(heading);
                if(actor==null){boss.setBoundingBox(geometry.bounds());boss.setOnGround(supported(world,boss));}
                begin(initialState(ability));return true;
            }
            public boolean tick(ExecutionSession<SpellMaledictus> s) {
                if(!geometry.present(cast,18) || !MonstrosityCasting.loadedArea(world,boss.position(),18)){
                    s.finish(ExecutionSession.EndReason.DENIED);return false;
                }
                ++age;++phaseAge;
                if(actor==null) {
                    // A detached position phantom has collision and gravity, but never becomes a world entity.
                    boss.move(MoverType.SELF,velocity);
                    velocity=new Vec3(boss.horizontalCollision?0:velocity.x,boss.verticalCollision?0:velocity.y,boss.horizontalCollision?0:velocity.z);
                    if(!boss.isFlying())velocity=velocity.add(0,-.08,0);
                    velocity=velocity.multiply(boss.onGround()?.6:.91,.98,boss.onGround()?.6:.91);
                }
                int state=boss.getAttackState();
                boolean tracking=switch(state){case 3->phaseAge<50;case 24->phaseAge<51;case 10->true;
                    case 11,12->phaseAge<18 || phaseAge>31;case 15,16,17->phaseAge<10 || phaseAge>28;
                    case 22,23->phaseAge<16;case 19,21->phaseAge<24;
                    case 18->phaseAge<15 || phaseAge>23 && phaseAge<29;default->false;};
                heading=geometry.steer(heading,tracking);pose();var input=velocity;
                goal.tick();
                // Keep native impulse calculation but restore shared actor/position aiming before native damage.
                var motion=boss.getDeltaMovement();if(actor!=null)geometry.pose(boss,heading);else fixedPose();
                boss.setDeltaMovement(motion);boss.aiStep();
                MaledictusCombatVisuals.tick(world,boss,actor,state,phaseAge);
                if(!applyMotion(s,input))return false;
                boolean transition=boss.getAttackState()!=state || phaseAge>=((NativeInternalGoal)goal).confluxMaledictus$duration()
                        || state==19 && ((MaledictusAttackAccess)(Object)boss).confluxMaledictus$combo();
                if(transition) {
                    // Never synthesize a landing merely because the airborne goal reached its timeout.
                    if((state==24 || state==4) && boss.getAttackState()==state){s.finish(ExecutionSession.EndReason.DENIED);return false;}
                    goal.stop();goal=null;int next=boss.getAttackState();
                    if(next==0)return true;
                    begin(next);
                }
                if(age>=260){s.finish(ExecutionSession.EndReason.DENIED);return false;}
                return false;
            }
            public void close(ExecutionSession<SpellMaledictus> s,ExecutionSession.EndReason reason) {
                if(reason!=ExecutionSession.EndReason.COMPLETED)boss.cancelChildren();
                if(goal!=null)goal.stop();MaledictusCasting.unlock(actor);
                if(started && actor!=null && !actor.isRemoved()) {
                    if(aerial){actor.setNoGravity(hadNoGravity);actor.fallDistance=0;}
                    actor.setDeltaMovement(actor.getDeltaMovement().multiply(.2,1,.2));actor.hurtMarked=true;
                }
                marker.discard();boss.discard();
            }
        };
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world,context,boss,262,
                BodyPhantomSync.wrap(ability,boss,driver),h->{},()->MaledictusCasting.enabled(ability),ExecutionScope.DIRECT));
    }
    private static boolean supported(ServerLevel world,Entity frame){return !world.noCollision(frame,frame.getBoundingBox().move(0,-.05,0));}
    private MaledictusCombatActions(){}
}
