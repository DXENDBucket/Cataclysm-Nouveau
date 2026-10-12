package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.client.particle.Options.*;
import com.github.L_Ender.cataclysm.entity.projectile.Player_Ceraunus_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.*;

/** Ally-directed actions: executor motion is distinct from spell ownership and payment. */
public final class ScyllaActions {
    private static final Set<UUID> ACTORS=new HashSet<>();
    private static final Set<UUID> LEAPING=new HashSet<>();
    public static boolean isLeaping(Entity actor) {return LEAPING.contains(actor.getUUID());}
    public static boolean isActing(Entity actor) {return ACTORS.contains(actor.getUUID());}
    public static boolean supports(Ability ability) {
        return switch(ability) {case THUNDER_COMBO,ANCHOR_SPIN,ANCHOR_LEAP,WATER_WALKING->true;default->false;};
    }
    public static boolean effect(Ability ability,ServerLevel world,CastContext cast,double amp,double duration) {
        if(cast.target() instanceof BlockHitResult && BodyPhantomAnimations.kind(ability)!=null)
            return PositionBodyActions.start(ability,world,cast,amp,duration);
        if(ability==Ability.WATER_WALKING) {
            var source=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
            if(source==null || source.entity()==null || !source.valid(world,cast))return false;
            return source.entity().addEffect(new MobEffectInstance(CataclysmEffects.WATER_WALKING,
                MonstrosityCasting.scaledTicks(CataclysmConfig.waterWalkingDuration(),duration),0,false,true),cast.caster());
        }
        var geometry=ActionGeometry.entity(world,cast,ability==Ability.ANCHOR_LEAP?14:12);if(geometry==null)return false;
        var actor=geometry.actor;
        if(actor.isPassenger() || !MonstrosityCasting.loadedArea(world,actor.position(),ability==Ability.ANCHOR_LEAP?14:12)) return false;
        var frame=cast.withExecutor(actor);
        var boss=new SpellScylla(world,frame,actor.position(),Resources.amplification(amp));
        Vec3 forward=geometry.direction(geometry.initialFacing);heading(boss,forward);
        if(ability==Ability.ANCHOR_LEAP) return leap(world,frame,actor,boss,forward,geometry);
        var aim=world.clip(new ClipContext(actor.getEyePosition(),actor.getEyePosition().add(forward.scale(32)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor));
        var marker=new ArmorStand(world,aim.getLocation().x,aim.getLocation().y,aim.getLocation().z);
        var entityHit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(world,actor,
                actor.getEyePosition(),aim.getLocation(),actor.getBoundingBox().expandTowards(forward.scale(32)).inflate(1),
                other->other instanceof LivingEntity && other.isAlive() && !Allies.areAllies(actor,other));
        boss.setTarget(entityHit!=null?(LivingEntity)entityHit.getEntity():marker);
        boolean combo=ability==Ability.THUNDER_COMBO;
        Goal goal=combo?boss.nativeGoal("WhipAndSpearGoal"):null;
        return start(world,frame,ability,boss,combo?108:55,new ExecutionDriver<SpellScylla>() {
            private int age;private boolean started;private Vec3 facing=geometry.initialFacing;
            public boolean occupiesCaster() {return actor==cast.caster();}
            public java.util.List<ExecutionClaim> claims(ExecutionSession<SpellScylla> s){return java.util.List.of(ExecutionClaim.action(actor));}
            public boolean start(ExecutionSession<SpellScylla> session) {
                started=true;ACTORS.add(actor.getUUID());if(goal!=null) goal.start();else boss.setAttackState(7);geometry.pose(boss,facing);return true;
            }
            public boolean tick(ExecutionSession<SpellScylla> session) {
                if(!geometry.present(cast,12)) return true;
                ++age;facing=geometry.steer(facing,!combo || age<21 || age>32 && age<104);
                marker.setPos(geometry.aimPoint(facing));
                if(!combo || age<21 || age>32 && age<104) {
                    var aimed=geometry.projectileTarget(facing);boss.setTarget(aimed==null?marker:aimed);
                }
                geometry.pose(boss,facing);boss.attackTicks=age;
                if(combo) {
                    goal.tick();geometry.pose(boss,facing);
                    if(age==21 || age==104) {boss.aiStep();strikeVisual(world,boss,actor,false);}
                    if(age==68) actor.swing(InteractionHand.MAIN_HAND);
                    return age>=105;
                }
                if(age>=12 && age<=52) boss.aiStep();
                if(age==29 || age==40 || age==51) strikeVisual(world,boss,actor,true);
                else if(age>=12 && age<=51 && age%4==0) world.sendParticles(new StormParticleOptions(.388F,.76F,.88F,5.5F,.7F,actor.getId()),
                        actor.getX(),actor.getY(),actor.getZ(),1,0,0,0,0);
                return age>=52;
            }
            public void close(ExecutionSession<SpellScylla> session,ExecutionSession.EndReason reason) {
                if(started){ACTORS.remove(actor.getUUID());if(goal!=null) goal.stop();}marker.discard();boss.discard();
            }
        });
    }
    private static boolean leap(ServerLevel world,CastContext cast,LivingEntity actor,SpellScylla boss,Vec3 forward,ActionGeometry geometry) {
        var selection=anchorAim(world,actor,forward);
        if(selection==null) return false;
        Vec3 target=selection.getLocation();
        var marker=new ArmorStand(world,target.x,target.y,target.z);boss.setTarget(marker);
        Goal jump=boss.nativeGoal("Scylla_Flying");
        var anchor=new Player_Ceraunus_Entity(world,actor);anchor.setBaseDamage(0);
        return start(world,cast,Ability.ANCHOR_LEAP,boss,160,new ExecutionDriver<SpellScylla>() {
            private int age;private boolean thrown,latched,released,started,hadNoGravity;
            private Vec3 facing=geometry.initialFacing;
            public boolean occupiesCaster() {return actor==cast.caster();}
            public java.util.List<ExecutionClaim> claims(ExecutionSession<SpellScylla> s){return java.util.List.of(ExecutionClaim.action(actor));}
            public boolean start(ExecutionSession<SpellScylla> session) {
                hadNoGravity=actor.isNoGravity();started=true;ACTORS.add(actor.getUUID());LEAPING.add(actor.getUUID());actor.setNoGravity(false);jump.start();boss.attackTicks=9;jump.tick();geometry.pose(boss,facing);
                actor.setDeltaMovement(boss.getDeltaMovement());actor.setOnGround(false);actor.hurtMarked=true;return true;
            }
            public boolean tick(ExecutionSession<SpellScylla> session) {
                if(!geometry.present(cast,1)) return true;
                age++;actor.fallDistance=0;facing=geometry.steer(facing,!thrown);
                geometry.pose(boss,facing);
                // FlyingMob.travel has no gravity even when noGravity=false. Its native travel still moves the body.
                if(actor instanceof FlyingMob && !latched) actor.setDeltaMovement(actor.getDeltaMovement().add(0,-.08,0));
                if(age==8) {
                    var state=(ManagedProjectile)anchor;state.confluxCataclysm$mode(2);
                    state.confluxCataclysm$impact(hit->{
                        if(hit instanceof EntityHitResult entity && Allies.areAllies(actor,entity.getEntity())) return;
                        anchor.setPos(hit.getLocation());anchor.setDeltaMovement(Vec3.ZERO);anchor.setNoPhysics(true);
                        state.confluxCataclysm$mode(1);state.confluxCataclysm$repeated(true);latched=true;
                        world.broadcastEntityEvent(anchor,(byte)73);
                    });
                    var launch=anchorAim(world,actor,geometry.direction(facing));
                    if(launch==null || actor.getEyePosition().distanceToSqr(launch.getLocation())<4) return true;
                    anchor.setPos(actor.getEyePosition());Vec3 aim=launch.getLocation().subtract(anchor.position());
                    // Re-sample after the jump. A straight hook cannot fall short and attach to incidental floor blocks.
                    anchor.setNoGravity(true);anchor.shoot(aim.x,aim.y,aim.z,2,0);
                    thrown=world.addFreshEntity(anchor);if(!thrown) return true;
                    session.trackProjectile(anchor);
                }
                if(thrown && anchor.isRemoved()) return true;
                if(thrown && !latched && actor.getDeltaMovement().y<0) {
                    // Keep a distant hook from losing its actor to landing before it reaches the selected wall.
                    var motion=actor.getDeltaMovement();actor.setDeltaMovement(motion.x,actor instanceof FlyingMob?0:.08,motion.z);actor.hurtMarked=true;
                }
                if(latched && !released) {
                    actor.setOnGround(false); // Pull movement must resolve before evaluating the landing burst.
                    Vec3 delta=anchor.position().subtract(actor.getEyePosition());
                    if(delta.lengthSqr()<2.25) {
                        released=true;actor.setDeltaMovement(0,-.6,0);actor.hurtMarked=true;
                    } else {
                        Vec3 step=delta.normalize().scale(Math.min(CataclysmConfig.anchorPullSpeed(),delta.length()));
                        var clip=world.clip(new ClipContext(actor.getEyePosition(),actor.getEyePosition().add(step),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor));
                        if(clip.getType()==HitResult.Type.BLOCK || actor.horizontalCollision) {
                            released=true;actor.setDeltaMovement(0,-.6,0);
                        } else actor.setDeltaMovement(step);
                        actor.hurtMarked=true;
                    }
                }
                if(age>10 && actor.onGround()) {
                    if(!latched) return true;
                    if(!MonstrosityCasting.loadedArea(world,actor.position(),14)) return true;
                    boss.setPos(actor.position());boss.setBoundingBox(actor.getBoundingBox());
                    boss.setAttackState(17);boss.attackTicks=1;boss.aiStep();
                    world.sendParticles(ModParticle.LIGHTNING_EXPLODE.get(),actor.getX(),actor.getY()+.03,actor.getZ(),1,0,0,0,0);
                    return true;
                }
                return age>=155;
            }
            public void close(ExecutionSession<SpellScylla> session,ExecutionSession.EndReason reason) {
                if(started){ACTORS.remove(actor.getUUID());LEAPING.remove(actor.getUUID());if(!actor.isRemoved())actor.setNoGravity(hadNoGravity);jump.stop();}
                var state=(ManagedProjectile)anchor;state.confluxCataclysm$impact(null);state.confluxCataclysm$resolving(false);
                anchor.discard();marker.discard();boss.discard();
                if(started && actor.level()==world && !actor.isRemoved()) {actor.setDeltaMovement(actor.getDeltaMovement().multiply(.2,1,.2));actor.hurtMarked=true;}
            }
        });
    }
    private static BlockHitResult anchorAim(ServerLevel world,LivingEntity actor,Vec3 direction) {
        Vec3 eye=actor.getEyePosition(),unit=direction.normalize();BlockHitResult hit=null;
        // Stop at the first surface; do not reject a nearby valid wall because distant chunks are absent.
        for(int i=0;i<CataclysmConfig.anchorRange();i++) {
            Vec3 from=eye.add(unit.scale(i)),to=eye.add(unit.scale(i+1));
            if(!world.hasChunkAt(BlockPos.containing(from)) || !world.hasChunkAt(BlockPos.containing(to))) return null;
            hit=world.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor));
            if(hit.getType()==HitResult.Type.BLOCK) return hit;
        }
        return null;
    }
    private static boolean start(ServerLevel world,CastContext cast,Ability ability,SpellScylla boss,int ticks,ExecutionDriver<SpellScylla> driver) {
        return ExecutionSessions.startReplacing(new ExecutionSession<>(world,cast,boss,ticks,dev.cataclysmnouveau.network.BodyPhantomSync.wrap(ability,boss,driver),hit->{},
                ()->GlyphCatalog.ALL.stream().filter(g->g instanceof dev.cataclysmnouveau.glyph.CataclysmEffectGlyph e
                        && e.ability==ability).allMatch(g->g.isEnabled()) && (cast.executor()==null || ExecutionAnchor.mayExecute(cast,cast.executor())),ExecutionScope.DIRECT));
    }
    private static void heading(SpellScylla boss,Vec3 direction) {
        boss.setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));boss.yBodyRot=boss.getYRot();boss.yRotO=boss.getYRot();
    }
    private static void strikeVisual(ServerLevel world,SpellScylla boss,LivingEntity actor,boolean spin) {
        actor.swing(InteractionHand.MAIN_HAND);
        Vec3 point=spin?actor.position():actor.position().add(boss.getLookAngle().multiply(1,0,1).normalize().scale(6));
        world.sendParticles(new ScyllaSwingParticleOptions(spin?2:1.2F,(float)Math.toRadians(-boss.yBodyRot),spin?-1.3F:0),
                point.x,point.y+actor.getBbHeight()*.5,point.z,1,0,0,0,0);
        if(!spin) {
            world.sendParticles(ModParticle.LIGHTNING_EXPLODE.get(),point.x,point.y+.03,point.z,1,0,0,0,0);
            world.sendParticles(new RingParticleOptions(0,(float)Math.PI/2,30,143,241,215,1,30,false,0),point.x,point.y+.02,point.z,1,0,0,0,0);
            var below=BlockPos.containing(point).below();
            if(world.hasChunkAt(below)) world.sendParticles(new net.minecraft.core.particles.BlockParticleOption(net.minecraft.core.particles.ParticleTypes.DUST_PILLAR,world.getBlockState(below)),
                    point.x,point.y+.2,point.z,40,1.5,.2,1.5,.07);
        }
    }
    private ScyllaActions() {}
}
