package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.lionfishapi.server.animation.Animation;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import dev.cataclysmnouveau.network.IgnisPhantomSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.util.List;
import java.util.Set;

/** Native attack goals run at a Core anchor; only an actual actor receives physical motion. */
final class IgnisActions {
    static boolean start(Ability ability,ServerLevel world,CastContext cast,double amp) {
        ExecutionAnchor anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null)return false;
        var geometry=new IgnisActionGeometry(world,cast,anchor);
        int area=ability==Ability.IGNIS_ULTIMATE?40:20;
        if(!geometry.present(cast,area))return false;
        LivingEntity actor=geometry.actor;
        boolean reinforced=ability==Ability.IGNIS_REINFORCED_SMASH;
        boolean airborne=ability==Ability.IGNIS_AIR_SMASH || reinforced;
        int launchTick=reinforced?84:19;
        var frame=cast.withExecutor(actor);
        var boss=new SpellIgnis(world,frame,ability,geometry.point(),geometry.initialFacing,amp);
        var aim=new ArmorStand(world,geometry.point().x,geometry.point().y,geometry.point().z);
        boss.setTarget(aim);
        boss.setAnimation(switch(ability){
            case IGNIS_AIR_SMASH->Ignis_Entity.SMASH_IN_AIR;
            case IGNIS_REINFORCED_SMASH->boss.getBossPhase()>0?Ignis_Entity.REINFORCED_SMASH_IN_AIR_SOUL:Ignis_Entity.REINFORCED_SMASH_IN_AIR;
            case IGNIS_EARTH_SHUDDERS->Ignis_Entity.EARTH_SHUDDERS_ATTACK;
            case IGNIS_ULTIMATE->Ignis_Entity.ULTIMATE_ATTACK;
            default->throw new IllegalArgumentException("Not an Ignis anchored action: "+ability);
        });
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world,frame,boss,reinforced?400:245,new ExecutionDriver<SpellIgnis>() {
            private int age,phaseAge;
            private Animation animation=boss.getAnimation();
            private final net.minecraft.world.entity.ai.goal.Goal goal=ability==Ability.IGNIS_ULTIMATE?boss.activeNativeGoal():boss.nativeGoal(
                    reinforced?"Reinforced_Air_Smash":airborne?"Air_Smash":"Earth_Shudders");
            private Vec3 heading=geometry.initialFacing;
            private boolean started,oldGravity;
            public List<ExecutionClaim> claims(ExecutionSession<SpellIgnis> s){return actor==null?List.of():List.of(ExecutionClaim.action(actor));}
            private void pose() {
                aim.setPos(geometry.aimPoint(heading));boss.pose(geometry.point(),heading);boss.setBoundingBox(geometry.bounds());
                boss.setOnGround(actor==null || actor.onGround());
                boss.setDeltaMovement(actor==null?Vec3.ZERO:actor.getDeltaMovement());boss.setAnimationTick(phaseAge);
            }
            public boolean start(ExecutionSession<SpellIgnis> s) {
                oldGravity=actor!=null && actor.isNoGravity();started=true;
                boss.projectileAim=()->geometry.projectileTarget(heading);
                if(actor!=null){IgnisCasting.lockActor(actor);if(actor instanceof Mob mob)mob.getNavigation().stop();if(airborne)actor.setNoGravity(false);}
                heading=geometry.desired(heading);pose();goal.start();IgnisPhantomSync.send(s.id(),actor,boss,false);return true;
            }
            public boolean tick(ExecutionSession<SpellIgnis> s) {
                if(!geometry.present(cast,area)){s.finish(ExecutionSession.EndReason.DENIED);return false;}
                ++age;++phaseAge;
                boolean landing=airborne && (animation==Ignis_Entity.SMASH || animation==Ignis_Entity.REINFORCED_SMASH || animation==Ignis_Entity.REINFORCED_SMASH_SOUL);
                if(!landing && canTurn(ability,phaseAge))heading=IgnisActionGeometry.turn(heading,geometry.desired(heading));
                pose();if(!landing)goal.tick();
                // Native goal yaw cannot override the server heading used for damage and the phantom.
                boss.pose(geometry.point(),heading);boss.setBoundingBox(geometry.bounds());
                if(boss.getAnimation()!=animation) {
                    Animation next=boss.getAnimation();
                    if(!airborne || !(reinforced?next==Ignis_Entity.REINFORCED_SMASH || next==Ignis_Entity.REINFORCED_SMASH_SOUL:next==Ignis_Entity.SMASH))return true;
                    goal.stop();boss.setAnimation(next);animation=next;phaseAge=0;
                    IgnisPhantomSync.send(s.id(),actor,boss,false);return false;
                }
                boss.aiStep();
                if(actor!=null && !landing) {
                    Vec3 motion=boss.getDeltaMovement();
                    if(!world.hasChunkAt(BlockPos.containing(actor.position().add(motion)))){s.finish(ExecutionSession.EndReason.WORLD_UNLOADED);return false;}
                    actor.setDeltaMovement(motion);actor.hurtMarked=true;
                    if(airborne && phaseAge==launchTick)actor.setOnGround(false);
                } else if(actor==null)boss.setDeltaMovement(Vec3.ZERO);
                if(airborne && actor!=null) {
                    actor.fallDistance=0;
                    if(!landing && phaseAge>launchTick && (actor instanceof FlyingMob || actor instanceof Player p && p.getAbilities().flying)) {
                        actor.setDeltaMovement(actor.getDeltaMovement().add(0,-.08,0));actor.hurtMarked=true;
                    }
                }
                boolean strike=landing?(reinforced?phaseAge==5 || phaseAge==46:Set.of(5,8,11,14).contains(phaseAge))
                        :ability==Ability.IGNIS_EARTH_SHUDDERS?Set.of(32,73,117).contains(phaseAge)
                        :ability==Ability.IGNIS_ULTIMATE?phaseAge==74:reinforced && Set.of(23,53).contains(phaseAge);
                if(strike) {
                    if(actor!=null)actor.swing(InteractionHand.MAIN_HAND);
                    Vec3 point=geometry.point();MonstrosityCasting.impactVisual(world,point.add(heading.scale(2)),true,true);
                    world.sendParticles(boss.getBossPhase()>0?ParticleTypes.SOUL_FIRE_FLAME:ParticleTypes.FLAME,
                            point.x,point.y+(actor==null?1.62:actor.getEyeHeight()),point.z,20,.8,.4,.8,.03);
                }
                if(age%4==0)IgnisPhantomSync.send(s.id(),actor,boss,false);
                if(airborne && !landing && age>=(reinforced?280:200)){s.finish(ExecutionSession.EndReason.EXPIRED);return false;}
                return (!airborne || landing) && phaseAge>=animation.getDuration();
            }
            public void close(ExecutionSession<SpellIgnis> s,ExecutionSession.EndReason reason) {
                if(started) {
                    IgnisPhantomSync.send(s.id(),actor,boss,true);
                    if(actor!=null)IgnisCasting.unlockActor(actor);
                    goal.stop();
                    if(reason!=ExecutionSession.EndReason.COMPLETED)boss.cancelChildren();
                    if(actor!=null && !actor.isRemoved()) {
                        if(airborne)actor.setNoGravity(oldGravity);
                        if(actor.level()==world){actor.setDeltaMovement(actor.getDeltaMovement().multiply(.2,1,.2));actor.hurtMarked=true;}
                    }
                }
                aim.discard();boss.discard();
            }
        },hit->{},()->enabled(ability) && geometry.present(cast,area),ExecutionScope.DIRECT));
    }
    private static boolean enabled(Ability ability){return GlyphCatalog.ALL.stream().anyMatch(g->g instanceof CataclysmEffectGlyph e && e.ability==ability && e.isEnabled());}
    private static boolean canTurn(Ability ability,int tick) {
        return switch(ability){
            case IGNIS_EARTH_SHUDDERS->tick<31 || tick>45 && tick<73 || tick>89 && tick<117;
            case IGNIS_ULTIMATE->tick<72;
            case IGNIS_REINFORCED_SMASH->tick<25 || tick>36 && tick<55 || tick>66;
            default->true;
        };
    }
    private IgnisActions(){}
}
