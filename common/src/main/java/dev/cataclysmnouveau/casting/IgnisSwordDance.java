package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.lionfishapi.server.animation.Animation;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.network.IgnisPhantomSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.Set;
import net.minecraft.world.phys.AABB;

/** One native attack frame supplies damage and visuals for either an entity or a fixed position. */
final class IgnisSwordDance {
    static boolean start(ServerLevel world,CastContext cast,double amp) {
        ExecutionAnchor anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null)return false;
        var geometry=new ActionGeometry(world,cast,anchor);
        if(!geometry.present(cast,20))return false;
        LivingEntity actor=anchor.entity();
        var context=cast.withExecutor(actor);
        final Vec3 initialFacing=geometry.initialFacing;
        var boss=new SpellIgnis(world,context,Ability.IGNIS_SWORD_COMBO,anchor.sample().origin(),initialFacing,amp);
        var aim=new ArmorStand(world,anchor.sample().origin().x,anchor.sample().origin().y,anchor.sample().origin().z);
        boss.setTarget(aim);boss.setAnimation(Ignis_Entity.COMBO1);
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world,context,boss,245,new ExecutionDriver<SpellIgnis>() {
            private int age,phaseAge;
            private Animation animation=Ignis_Entity.COMBO1;
            private net.minecraft.world.entity.ai.goal.Goal goal=boss.nativeGoal("Combo1");
            private Vec3 heading=initialFacing;
            private boolean started;
            public boolean occupiesCaster(){return actor!=null && actor==cast.caster();}
            public java.util.List<ExecutionClaim> claims(ExecutionSession<SpellIgnis> s){return actor==null?java.util.List.of():java.util.List.of(ExecutionClaim.action(actor));}
            private void aim() {
                Vec3 point=anchor.sample().origin();
                // The native Goal still drives animation, lunges and attacks. Player aim is the camera,
                // rather than an enemy captured by a one-time raycast. The helper is never spawned.
                aim.setPos(geometry.aimPoint(heading));
                boss.pose(point,heading);boss.setBoundingBox(geometry.bounds());
                boss.setOnGround(actor==null || actor.onGround());
                boss.setDeltaMovement(actor==null?Vec3.ZERO:actor.getDeltaMovement());
                boss.setAnimationTick(phaseAge);
            }
            public boolean start(ExecutionSession<SpellIgnis> s) {
                started=true;
                if(actor!=null){IgnisCasting.lockActor(actor);if(actor instanceof Mob mob)mob.getNavigation().stop();}
                heading=geometry.desired(heading);aim();goal.start();IgnisPhantomSync.send(s.id(),actor,boss,false);return true;
            }
            public boolean tick(ExecutionSession<SpellIgnis> s) {
                if(!geometry.present(cast,20)){s.finish(ExecutionSession.EndReason.DENIED);return false;}
                ++age;++phaseAge;
                if(actor!=null && canTurn(animation,phaseAge))heading=geometry.steer(heading,true);
                aim();goal.tick();
                // Native Goals can change yaw themselves. Damage and the visual must use the same
                // bounded/locked heading, even when their native target prediction changes.
                boss.pose(anchor.sample().origin(),heading);boss.setBoundingBox(geometry.bounds());
                if(actor instanceof Player && animation==Ignis_Entity.COMBO2 && phaseAge<27)
                    boss.setTargetPosition(anchor.sample().origin().add(heading.scale(6)));
                if(boss.getAnimation()!=animation) {
                    if(boss.getAnimation()!=Ignis_Entity.COMBO2 || animation!=Ignis_Entity.COMBO1)return true;
                    goal.stop();animation=Ignis_Entity.COMBO2;boss.setAnimation(animation);phaseAge=0;
                    aim();goal=boss.nativeGoal("Combo2");goal.start();
                    IgnisPhantomSync.send(s.id(),actor,boss,false);return false;
                }
                boss.aiStep();
                if(actor!=null) {
                    Vec3 motion=boss.getDeltaMovement();
                    if(!world.hasChunkAt(BlockPos.containing(actor.position().add(motion))))return true;
                    actor.setDeltaMovement(motion);actor.hurtMarked=true;
                } else boss.setDeltaMovement(Vec3.ZERO);
                boolean strike=animation==Ignis_Entity.COMBO1?Set.of(19,38,61,76).contains(phaseAge)
                        :Set.of(32,59,74,108,110,112,114,116).contains(phaseAge);
                if(strike) {
                    if(actor!=null)actor.swing(InteractionHand.MAIN_HAND);
                    Vec3 point=anchor.sample().origin();
                    MonstrosityCasting.impactVisual(world,point.add(heading.scale(2)),true,true);
                    world.sendParticles(boss.getBossPhase()>0?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME,
                            point.x,point.y+(actor==null?1.62:actor.getEyeHeight()),point.z,20,.8,.4,.8,.03);
                }
                if(age%4==0)IgnisPhantomSync.send(s.id(),actor,boss,false);
                return phaseAge>=animation.getDuration();
            }
            public void close(ExecutionSession<SpellIgnis> s,ExecutionSession.EndReason reason) {
                if(started)IgnisPhantomSync.send(s.id(),actor,boss,true);
                if(started && actor!=null)IgnisCasting.unlockActor(actor);
                if(started)goal.stop();aim.discard();boss.discard();
                if(started && actor!=null && actor.level()==world && !actor.isRemoved()) {
                    actor.setDeltaMovement(actor.getDeltaMovement().multiply(.2,1,.2));actor.hurtMarked=true;
                }
            }
        },hit->{},()->GlyphCatalog.IGNIS_SWORD_COMBO.isEnabled(),ExecutionScope.DIRECT));
    }
    private static boolean canTurn(Animation animation,int tick) {
        return animation==Ignis_Entity.COMBO1?tick<17 || tick>44 && tick<59
                :tick<27 || tick>43 && tick<57 || tick>61 && tick<72 || tick>77 && tick<105;
    }
    private IgnisSwordDance(){}
}
