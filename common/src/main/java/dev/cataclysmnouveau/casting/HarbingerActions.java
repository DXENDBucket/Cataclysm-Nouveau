package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.init.ModSounds;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import dev.cataclysmnouveau.mixin.HarbingerAttackAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.Supplier;

/** Native goals/payloads own attack behavior; Core owns execution, roles and cancellation. */
public final class HarbingerActions {
    private static final Set<UUID> CHARGING=new HashSet<>();
    public static boolean isCharging(Entity actor){return CHARGING.contains(actor.getUUID());}
    public static boolean supports(Ability a){return switch(a){case HARBINGER_CHARGE,HOWITZER_VOLLEY,MISSILE_SALVO,WITHER_SMOKE->true;default->false;};}
    private static boolean enabled(Ability a){return GlyphCatalog.ALL.stream().anyMatch(g->g instanceof CataclysmEffectGlyph e && e.ability==a && e.isEnabled());}
    public static boolean effect(Ability a,ServerLevel world,CastContext cast,double amp,double duration) {
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS)return false;
        if(a==Ability.HARBINGER_CHARGE)return charge(world,cast,amp);
        if(a==Ability.WITHER_SMOKE) {
            Vec3 point=cast.target() instanceof EntityHitResult hit?hit.getEntity().position():cast.origin();
            if(cast.target() instanceof BlockHitResult block)point=point.add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(.02));
            if(!world.isInWorldBounds(BlockPos.containing(point)) || !world.hasChunkAt(BlockPos.containing(point)))return false;
            var cloud=new Wither_Smoke_Effect_Entity(world,point.x,point.y,point.z);cloud.setOwner(cast.damageOwner());cloud.setRadius(3);cloud.setWaitTime(5);
            int ticks=MonstrosityCasting.scaledTicks(300,duration);cloud.setDuration(ticks);cloud.setRadiusPerTick(-3F/ticks);
            return HarbingerCasting.cloud(world,cloud,cast,Resources.amplification(amp),a,()->world.addFreshEntity(cloud));
        }
        return volley(a,world,cast,amp);
    }
    private static Vec3 loadedAim(ServerLevel world,LivingEntity actor,Vec3 point,Vec3 facing) {
        Vec3 end=point;
        for(int i=1;i<=32;i++){Vec3 next=point.add(facing.scale(i));if(!world.hasChunkAt(BlockPos.containing(next)))break;end=next;}
        return world.clip(new ClipContext(point,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor)).getLocation();
    }
    private static ArmorStand marker(ServerLevel world,Vec3 point){return new ArmorStand(world,point.x,point.y,point.z);}
    private static boolean volley(Ability a,ServerLevel world,CastContext cast,double amp) {
        LivingEntity host=null,target=null;
        if(cast.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living) {
            if(HarbingerCasting.allied(cast,living))host=living;
            else {host=cast.caster();target=living;}
        }
        final LivingEntity emitter=host;
        Vec3 point=host==null?cast.origin():host.getEyePosition();
        if(cast.target() instanceof BlockHitResult block)point=point.add(Vec3.atLowerCornerOf(block.getDirection().getNormal()).scale(.02));
        Vec3 forward=host==null?MonstrosityCasting.direction(cast):host.getLookAngle();
        if(!world.hasChunkAt(BlockPos.containing(point)))return false;
        if(target==null)target=HarbingerCasting.target(world,point,forward,cast);
        if(a==Ability.MISSILE_SALVO && target==null)return false;
        if(target==null)target=marker(world,loadedAim(world,cast.caster(),point,forward));
        if(!target.isAlive() || target.level()!=world || !world.hasChunkAt(target.blockPosition()))return false;
        var frame=cast.withExecutor(host==null?cast.caster():host);
        var boss=new SpellHarbinger(world,frame,a,point,a==Ability.HOWITZER_VOLLEY?2.6:3.8,amp);boss.setTarget(target);boss.pose(point,forward);
        var goal=boss.nativeGoal(a==Ability.HOWITZER_VOLLEY?"LaunchGoal":boss.isPowered()?"MissileLaunchGoal2":"MissileLaunchGoal");
        boss.setAnimation(a==Ability.HOWITZER_VOLLEY?The_Harbinger_Entity.LAUNCH_ANIAMATION:boss.isPowered()?The_Harbinger_Entity.MISSILE_FIRE_FAST_ANIAMATION:The_Harbinger_Entity.MISSILE_FIRE_ANIAMATION);
        final Vec3 fixed=point;int end=a==Ability.HOWITZER_VOLLEY?20:boss.isPowered()?80:107;
        return ExecutionSessions.start(new ExecutionSession<>(world,frame,boss,end+2,new ExecutionDriver<SpellHarbinger>() {
            private int age;
            public boolean start(ExecutionSession<SpellHarbinger> s){goal.start();return true;}
            public boolean tick(ExecutionSession<SpellHarbinger> s) {
                var selected=boss.getTarget();Vec3 origin=emitter==null?fixed:emitter.getEyePosition();
                if(selected==null || !selected.isAlive() || selected.level()!=world || !world.hasChunkAt(selected.blockPosition())
                        || !world.hasChunkAt(BlockPos.containing(origin)) || selected.position().distanceToSqr(origin)>64*64)return true;
                Vec3 heading=selected.position().subtract(origin).normalize();boss.pose(origin,heading);boss.setAnimationTick(++age);
                if(a==Ability.MISSILE_SALVO && age==24)world.playSound(null,BlockPos.containing(origin),ModSounds.HARBINGER_PREPARE.get(),SoundSource.PLAYERS,1,1);
                goal.tick();return age>=end;
            }
            public void close(ExecutionSession<SpellHarbinger> s,ExecutionSession.EndReason reason){goal.stop();boss.discard();}
        },hit->{},()->enabled(a),ExecutionScope.DIRECT));
    }
    /** Goal-produced children have independent lifetimes, without repeating fees or Ars continuations. */
    public static boolean child(ServerLevel world,Entity child,SpellHarbinger boss,Supplier<Boolean> original) {
        if(!(child instanceof Projectile projectile) || !HarbingerCasting.nativePayload(child))return original.get();
        var state=(ManagedProjectile)child;state.confluxCataclysm$mode(2);state.confluxCataclysm$amplification(boss.amplification);state.confluxCataclysm$sourceAbility(boss.ability);
        projectile.setOwner(boss.cast.damageOwner());NativeDamageContexts.bind(child,boss.cast,boss.amplification);
        if(!world.hasChunkAt(child.blockPosition()))return false;
        return ExecutionSessions.start(new ExecutionSession<>(world,boss.cast,projectile,400,new ExecutionDriver<Projectile>() {
            public boolean start(ExecutionSession<Projectile> s){return original.get();}
            public boolean tick(ExecutionSession<Projectile> s){return child.isRemoved() || !s.world().hasChunkAt(child.blockPosition()) || !s.world().hasChunkAt(BlockPos.containing(child.position().add(child.getDeltaMovement())));}
            public void close(ExecutionSession<Projectile> s,ExecutionSession.EndReason reason){child.discard();}
        },hit->{},()->enabled(boss.ability),ExecutionScope.DIRECT));
    }
    private static boolean charge(ServerLevel world,CastContext cast,double amp) {
        if(!(cast.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity actor)
                || !actor.isAlive() || actor.level()!=world || !Allies.areAllies(cast.caster(),actor) || actor.isPassenger()
                || CHARGING.contains(actor.getUUID()) || ScyllaActions.isLeaping(actor) || ExecutionSessions.isCasting(actor))return false;
        Vec3 forward=actor.getLookAngle();var frame=cast.withExecutor(actor);
        var boss=new SpellHarbinger(world,frame,Ability.HARBINGER_CHARGE,actor.position(),0,amp);
        var target=marker(world,loadedAim(world,actor,actor.getEyePosition(),forward).add(0,2-actor.getEyeHeight(),0));
        boss.setTarget(target);boss.setAnimation(The_Harbinger_Entity.CHARGE_ANIMATION);
        var goal=boss.nativeGoal("ChargeGoal");
        return ExecutionSessions.start(new ExecutionSession<>(world,frame,boss,138,new ExecutionDriver<SpellHarbinger>() {
            private int age;private Vec3 impulse=Vec3.ZERO;private boolean stopped;
            public boolean occupiesCaster(){return actor==cast.caster();}
            public boolean start(ExecutionSession<SpellHarbinger> s){CHARGING.add(actor.getUUID());goal.start();return true;}
            public boolean tick(ExecutionSession<SpellHarbinger> s) {
                if(!world.hasChunkAt(actor.blockPosition()))return true;
                if(++age>=45) {
                    boss.repeatRequested=false;boss.acceptingRepeat=true;try{goal.stop();}finally{boss.acceptingRepeat=false;}stopped=true;
                    if(!boss.repeatRequested)return true;
                    age=0;impulse=Vec3.ZERO;boss.setAnimationTick(0);boss.setIsCharge(false);goal.start();stopped=false;return false;
                }
                if(age<24) {
                    Vec3 aim=loadedAim(world,actor,actor.getEyePosition(),actor.getLookAngle());
                    target.setPos(aim.add(0,2-actor.getEyeHeight(),0));
                }
                boss.pose(actor.position(),actor.getLookAngle());boss.setBoundingBox(actor.getBoundingBox());boss.setAnimationTick(age);boss.tickCount=age;
                goal.tick();
                if(age==24)impulse=boss.getDeltaMovement();
                if(boss.getIsCharge()) {
                    if(actor.horizontalCollision || !world.hasChunkAt(BlockPos.containing(actor.position().add(impulse))))return true;
                    Vec3 motion=Math.abs(impulse.y)<1e-5?new Vec3(impulse.x,actor.getDeltaMovement().y,impulse.z):impulse;
                    actor.setDeltaMovement(motion);actor.hurtMarked=true;
                    ((HarbingerAttackAccess)(Object)boss).confluxHarbinger$collision();
                    if(age%2==0)world.sendParticles(ParticleTypes.SMOKE,actor.getX(),actor.getEyeY(),actor.getZ(),10,.5,.25,.5,.02);
                    if(age==24)world.playSound(null,actor.blockPosition(),ModSounds.HARBINGER_CHARGE.get(),SoundSource.PLAYERS,2,.65F);
                } else if(age<24 && age%4==0)world.sendParticles(ParticleTypes.SMOKE,actor.getX(),actor.getEyeY(),actor.getZ(),3,.2,.2,.2,.01);
                return false;
            }
            public void close(ExecutionSession<SpellHarbinger> s,ExecutionSession.EndReason reason) {
                CHARGING.remove(actor.getUUID());boss.setIsCharge(false);if(!stopped)goal.stop();boss.discard();
                if(actor.level()==world && !actor.isRemoved()){actor.setDeltaMovement(actor.getDeltaMovement().multiply(.2,1,.2));actor.hurtMarked=true;}
            }
        },impact->{},()->enabled(Ability.HARBINGER_CHARGE),ExecutionScope.DIRECT));
    }
    private HarbingerActions() {}
}
