package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.Flame_Strike_Entity;
import com.github.L_Ender.cataclysm.util.CustomExplosion.IgnisExplosion;
import com.github.L_Ender.lionfishapi.server.animation.Animation;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import dev.cataclysmnouveau.mixin.IgnisAttackAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.Supplier;

/** Native Ignis attacks and explosion visuals; Core owns context, fees and lifetime. */
public final class IgnisCasting {
    private static final Set<UUID> ACTORS=new HashSet<>();
    public static boolean isActing(Entity actor){return ACTORS.contains(actor.getUUID());}
    static void lockActor(Entity actor){ACTORS.add(actor.getUUID());}
    static void unlockActor(Entity actor){ACTORS.remove(actor.getUUID());}
    public static boolean projectile(Ability a){return a==Ability.IGNIS_FIREBALL || a==Ability.IGNIS_ABYSS_FIREBALL;}
    public static boolean supports(Ability a){return switch(a){case IGNIS_BLADE_FIREBALLS,IGNIS_AIR_SMASH,IGNIS_SWORD_COMBO,
            IGNIS_FIREBALL_ARRAY,IGNIS_EARTH_SHUDDERS,IGNIS_REINFORCED_SMASH,IGNIS_ULTIMATE,IGNIS_FLAME_STRIKE->true;default->false;};}
    private static boolean enabled(Ability a){return GlyphCatalog.ALL.stream().anyMatch(g->g instanceof CataclysmEffectGlyph e && e.ability==a && e.isEnabled());}
    public static int phase(LivingEntity caster) {
        double fraction=caster.getHealth()/caster.getMaxHealth();
        return fraction<=1.0/3?2:fraction<=2.0/3?1:0;
    }
    public static CMAbstractHurtingProjectile create(Ability a,ServerLevel world,CastContext cast,Vec3 direction,LivingEntity target,double amp) {
        CMAbstractHurtingProjectile shot;
        if(a==Ability.IGNIS_ABYSS_FIREBALL) {
            var fire=new Ignis_Abyss_Fireball_Entity(world,cast.damageOwner(),direction.x,direction.y,direction.z);
            fire.setFired(true);shot=fire;
        } else {
            var fire=new Ignis_Fireball_Entity(world,cast.damageOwner(),direction.x,direction.y,direction.z);
            fire.setSoul(phase(cast.caster())>0);fire.setFired(true);shot=fire;
        }
        Vec3 unit=direction.normalize();
        double power=shot instanceof Ignis_Fireball_Entity fire && fire.isSoul()?.25:.2;
        shot.xPower=unit.x*power;shot.yPower=unit.y*power;shot.zPower=unit.z*power;
        shot.shoot(unit.x,unit.y,unit.z,.25F,0);
        ((IgnisProjectile)shot).confluxIgnis$aim(target,1);
        ((ManagedProjectile)shot).confluxCataclysm$sourceAbility(a);
        return NativeDamageContexts.bind(shot,cast,amp);
    }
    public static void accelerate(CMAbstractHurtingProjectile shot,double factor,LivingEntity target) {
        shot.xPower*=factor;shot.yPower*=factor;shot.zPower*=factor;
        ((IgnisProjectile)shot).confluxIgnis$aim(target,factor);
    }
    public static void steer(CMAbstractHurtingProjectile shot,LivingEntity target,double factor) {
        if(target==null || !target.isAlive() || target.level()!=shot.level())return;
        if(shot instanceof Ignis_Abyss_Fireball_Entity abyss && abyss.getTotalBounces()!=0)return;
        Vec3 direction=target.position().add(0,target.getBbHeight()*.5,0).subtract(shot.position()).normalize();
        if(direction.lengthSqr()<.01)return;
        double speed=shot instanceof Ignis_Fireball_Entity fire && fire.isSoul()?.25:.2;
        shot.xPower=direction.x*speed*factor;shot.yPower=direction.y*speed*factor;shot.zPower=direction.z*speed*factor;
    }
    public static IgnisExplosion explosion(Level world,Entity source,double x,double y,double z,float radius) {
        var cast=NativeDamageContexts.context(source);
        return new IgnisExplosion(world,source,world.damageSources().explosion(source,cast.damageOwner()),new ExplosionDamageCalculator(){
            public boolean shouldDamageEntity(Explosion e,Entity target){return !HarbingerCasting.allied(cast,target);}
            public float getKnockbackMultiplier(Entity target){return HarbingerCasting.allied(cast,target)?0:super.getKnockbackMultiplier(target);}
            public float getEntityDamageAmount(Explosion e,Entity target){return Resources.augmentedDamage(super.getEntityDamageAmount(e,target),NativeDamageContexts.amplification(source));}
        },x,y,z,radius,!(source instanceof Flame_Strike_Entity) && CataclysmConfig.ignisTerrainFire(),Explosion.BlockInteraction.KEEP);
    }
    public static boolean effect(Ability a,ServerLevel world,CastContext cast,double amp) {
        return effect(a,world,cast,amp,0);
    }
    public static boolean effect(Ability a,ServerLevel world,CastContext cast,double amp,double duration) {
        if(cast.target()==null || cast.target().getType()==HitResult.Type.MISS)return false;
        if(a==Ability.IGNIS_FLAME_STRIKE)return flame(world,cast,amp,duration);
        if(a==Ability.IGNIS_SWORD_COMBO)return IgnisSwordDance.start(world,cast,amp);
        if(a==Ability.IGNIS_BLADE_FIREBALLS || a==Ability.IGNIS_FIREBALL_ARRAY)return blades(a,world,cast,amp);
        return IgnisActions.start(a,world,cast,amp);
    }
    private static LivingEntity marker(ServerLevel world,Vec3 point){return new ArmorStand(world,point.x,point.y,point.z);}
    private static Vec3 horizontal(Vec3 direction){var h=direction.multiply(1,0,1).normalize();return h.lengthSqr()<.01?new Vec3(0,0,1):h;}
    private static boolean valid(ServerLevel world,LivingEntity actor,LivingEntity target) {
        return target!=null && target.isAlive() && !target.isRemoved() && target.level()==world
            && world.hasChunkAt(target.blockPosition()) && actor.distanceToSqr(target)<=32*32;
    }
    private static boolean flame(ServerLevel world,CastContext cast,double amp,double duration) {
        Vec3 point=NativePayloads.groundPoint(world,cast);
        if(point==null || !MonstrosityCasting.loadedArea(world,point,3))return false;
        Vec3 facing=horizontal(MonstrosityCasting.direction(cast));
        var boss=new SpellIgnis(world,cast,Ability.IGNIS_FLAME_STRIKE,point,facing,amp);
        ((IgnisAttackAccess)(Object)boss).confluxIgnis$flame(point.x,point.z,point.y-1,point.y,
                (float)Math.atan2(facing.x,facing.z),MonstrosityCasting.scaledTicks(60,duration),0,10,2,boss.getBossPhase()>0);
        boss.discard();
        return boss.spawnedFlames>0;
    }
    private static boolean blades(Ability a,ServerLevel world,CastContext cast,double amp) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);if(anchor==null || !anchor.valid(world,cast))return false;
        LivingEntity host=anchor.entity(),target=null;
        Vec3 point=anchor.sample().origin(),facing=anchor.sample().direction();
        if(!MonstrosityCasting.loadedArea(world,point,8))return false;
        if(target==null)target=HarbingerCasting.target(world,host==null?point:host.getEyePosition(),facing,cast);
        if(target==null)target=marker(world,point.add(facing.normalize().scale(16)).add(0,1,0));
        final LivingEntity emitter=host;
        var frame=cast.withExecutor(host);
        var boss=new SpellIgnis(world,frame,a,point,facing,amp);boss.setTarget(target);
        if(a==Ability.IGNIS_FIREBALL_ARRAY)boss.setAnimation(Ignis_Entity.MAGIC_ATTACK);
        return ExecutionSessions.start(new ExecutionSession<>(world,frame,boss,58,new ExecutionDriver<SpellIgnis>() {
            private int age;
            public boolean start(ExecutionSession<SpellIgnis> s){return true;}
            public boolean tick(ExecutionSession<SpellIgnis> s) {
                if(!boss.getTarget().isAlive() || boss.getTarget().level()!=world)return true;
                Vec3 origin=emitter==null?point:emitter.position();
                if(!MonstrosityCasting.loadedArea(world,origin,8))return true;
                boss.pose(origin,boss.getTarget().position().subtract(origin));
                ++age;
                var access=(IgnisAttackAccess)(Object)boss;
                if(a==Ability.IGNIS_FIREBALL_ARRAY){boss.setAnimationTick(age);boss.aiStep();return age>=6;}
                if(age==21)access.confluxIgnis$blade(2,-1.5F,5,30);
                if(age==23)access.confluxIgnis$blade(3,0,4,28);
                if(age==25)access.confluxIgnis$blade(2,1.5F,3,26);
                return age>=56;
            }
            public void close(ExecutionSession<SpellIgnis> s,ExecutionSession.EndReason reason){boss.discard();}
        },hit->{},()->enabled(a),ExecutionScope.DIRECT));
    }
    public static boolean child(ServerLevel world,Entity child,SpellIgnis boss,Supplier<Boolean> original) {
        if(child instanceof Flame_Strike_Entity flame) {
            if(!MonstrosityCasting.loadedArea(world,flame.position(),(int)Math.ceil(flame.getRadius())))return false;
            flame.setOwner(boss.cast.damageOwner());NativeDamageContexts.bind(flame,boss.cast,boss.amplification);
            boolean started=ExecutionSessions.start(new ExecutionSession<>(world,boss.cast,flame,flame.getDuration()+flame.getWaitTime()+400,new ExecutionDriver<Flame_Strike_Entity>() {
                public boolean start(ExecutionSession<Flame_Strike_Entity> s){return original.get();}
                public boolean tick(ExecutionSession<Flame_Strike_Entity> s){return flame.isRemoved() || boss.childrenCancelled || !world.hasChunkAt(flame.blockPosition());}
                public void close(ExecutionSession<Flame_Strike_Entity> s,ExecutionSession.EndReason reason){boss.children.remove(flame);flame.discard();}
            },hit->{},()->enabled(boss.ability),ExecutionScope.DIRECT));
            if(started){boss.spawnedFlames++;boss.children.add(flame);}
            return started;
        }
        if(!(child instanceof CMAbstractHurtingProjectile shot))return original.get();
        if(!world.hasChunkAt(child.blockPosition()))return false;
        shot.setOwner(boss.cast.damageOwner());
        ((IgnisProjectile)shot).confluxIgnis$aim(boss.projectileAim==null?boss.getTarget():boss.projectileAim.get(),1);
        var state=(ManagedProjectile)shot;state.confluxCataclysm$mode(2);state.confluxCataclysm$sourceAbility(boss.ability);
        NativeDamageContexts.bind(shot,boss.cast,boss.amplification);
        boolean started=ExecutionSessions.start(new ExecutionSession<>(world,boss.cast,shot,400,new ExecutionDriver<CMAbstractHurtingProjectile>() {
            public boolean start(ExecutionSession<CMAbstractHurtingProjectile> s){return original.get();}
            public boolean tick(ExecutionSession<CMAbstractHurtingProjectile> s){return shot.isRemoved() || boss.childrenCancelled || !world.hasChunkAt(shot.blockPosition()) || !world.hasChunkAt(BlockPos.containing(shot.position().add(shot.getDeltaMovement())));}
            public void close(ExecutionSession<CMAbstractHurtingProjectile> s,ExecutionSession.EndReason reason){boss.children.remove(shot);shot.discard();}
        },hit->{},()->enabled(boss.ability),ExecutionScope.DIRECT));
        if(started)boss.children.add(shot);return started;
    }
    private IgnisCasting() {}
}
