package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Void_Vortex_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Void_Rune_Entity;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.glyph.CataclysmEffectGlyph;
import dev.cataclysmnouveau.mixin.GuardianAttackAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import java.util.*;

public final class GuardianCasting {
    private static final Set<UUID> PUNCHING=new HashSet<>();
    public static boolean isPunching(Entity actor){return PUNCHING.contains(actor.getUUID()) || GuardianActions.isActing(actor);}
    public static boolean rejectDamage(Entity source,Entity target) {
        if(!(source instanceof SpellGuardian || source instanceof GuardianBullet || source instanceof GuardianChild child && child.confluxGuardian$managed()))return false;
        var cast=NativeDamageContexts.context(source);return cast!=null && HarbingerCasting.allied(cast,target);
    }
    public static boolean supports(Ability a){return a==Ability.VOID_VORTEX || a==Ability.GUARDIAN_STOMP || a==Ability.GRAVITY_PUNCH || GuardianActions.supports(a);}
    private static boolean enabled(Ability a){return GlyphCatalog.ALL.stream().anyMatch(g->g instanceof CataclysmEffectGlyph e && e.ability==a && e.isEnabled());}
    public static boolean effect(Ability a,ServerLevel world,CastContext cast,double amp,double duration) {
        if(GuardianActions.supports(a))return GuardianActions.effect(a,world,cast,amp);
        if(a==Ability.GRAVITY_PUNCH)return punch(world,cast,amp);
        Vec3 point=NativePayloads.groundPoint(world,cast);if(point==null)return false;
        if(a==Ability.VOID_VORTEX) {
            var cloud=new Void_Vortex_Entity(world,point.x,point.y,point.z,0,cast.damageOwner(),MonstrosityCasting.scaledTicks(CataclysmConfig.guardianVortexLifetime(),duration));
            return cloud(world,cloud,cast,amp,Ability.VOID_VORTEX);
        }
        if(a!=Ability.GUARDIAN_STOMP || !MonstrosityCasting.loadedArea(world,point,18))return false;
        Vec3 direction=MonstrosityCasting.direction(cast).multiply(1,0,1).normalize();
        if(direction.lengthSqr()<.01)direction=new Vec3(0,0,1);
        var boss=new SpellGuardian(world,cast,point,direction,amp);
        var marker=new ArmorStand(world,point.x+direction.x*16,point.y,point.z+direction.z*16);boss.setTarget(marker);
        return ExecutionSessions.start(new ExecutionSession<>(world,cast,boss,boss.getIsHelmetless()?65:35,new ExecutionDriver<SpellGuardian>() {
            private int age;
            public boolean start(ExecutionSession<SpellGuardian> s){return true;}
            public boolean tick(ExecutionSession<SpellGuardian> s) {
                if(!MonstrosityCasting.loadedArea(world,point,18))return true;
                ++age;
                if(age==32 || boss.getIsHelmetless() && (age==53 || age==62)) {
                    ((GuardianAttackAccess)(Object)boss).confluxGuardian$stomp();
                    MonstrosityCasting.impactVisual(world,point,false,true);
                }
                return age>=(boss.getIsHelmetless()?63:33);
            }
            public void close(ExecutionSession<SpellGuardian> s,ExecutionSession.EndReason reason){boss.discard();}
        },h->{},()->enabled(a),ExecutionScope.DIRECT));
    }
    private static boolean punch(ServerLevel world,CastContext cast,double amp) {
        if(!(cast.target() instanceof EntityHitResult hit) || !(hit.getEntity() instanceof LivingEntity actor)
                || !actor.isAlive() || actor.level()!=world || !HarbingerCasting.allied(cast,actor)
                || actor.isPassenger() || isPunching(actor) || ScyllaActions.isActing(actor) || HarbingerActions.isCharging(actor) || ExecutionSessions.isCasting(actor))return false;
        var frame=cast.withExecutor(actor);
        var boss=new SpellGuardian(world,frame,actor.position(),actor.getLookAngle(),amp);
        boss.setAnimation(Ender_Guardian_Entity.GUARDIAN_RIGHT_STRONG_ATTACK);
        return ExecutionSessions.start(new ExecutionSession<>(world,frame,boss,61,new ExecutionDriver<SpellGuardian>() {
            private int age;
            public boolean occupiesCaster(){return actor==cast.caster();}
            public boolean start(ExecutionSession<SpellGuardian> s){PUNCHING.add(actor.getUUID());return true;}
            public boolean tick(ExecutionSession<SpellGuardian> s) {
                if(!MonstrosityCasting.loadedArea(world,actor.position(),12))return true;
                boss.pose(actor.position(),actor.getLookAngle());boss.setBoundingBox(actor.getBoundingBox());boss.setAnimationTick(++age);
                boss.aiStep();
                var front=actor.position().add(actor.getLookAngle().multiply(1,0,1).normalize().scale(2.2));
                if(age<24 && age%3==0)world.sendParticles(ParticleTypes.PORTAL,front.x,front.y+.3,front.z,12,.8,.5,.8,.25);
                if(age==29){actor.swing(net.minecraft.world.InteractionHand.MAIN_HAND);MonstrosityCasting.impactVisual(world,front,false,true);}
                return age>=60;
            }
            public void close(ExecutionSession<SpellGuardian> s,ExecutionSession.EndReason reason){PUNCHING.remove(actor.getUUID());boss.discard();}
        },h->{},()->enabled(Ability.GRAVITY_PUNCH),ExecutionScope.DIRECT));
    }
    public static boolean cloud(ServerLevel world,Entity child,CastContext cast,double amp,Ability source) {
        NativeDamageContexts.bind(child,cast,amp);
        if(child instanceof GuardianChild state)state.confluxGuardian$managed(true);
        int lifetime=child instanceof Void_Vortex_Entity vortex?vortex.getLifespan()+5:200;
        return ExecutionSessions.start(new ExecutionSession<>(world,cast,child,lifetime,new ExecutionDriver<Entity>() {
            public boolean start(ExecutionSession<Entity> s){return world.addFreshEntity(child);}
            public boolean tick(ExecutionSession<Entity> s){return child.isRemoved() || !world.hasChunkAt(child.blockPosition());}
            public void close(ExecutionSession<Entity> s,ExecutionSession.EndReason reason){child.discard();}
        },h->{},()->enabled(source),ExecutionScope.DIRECT));
    }
    public static boolean rune(ServerLevel world,Entity child,SpellGuardian boss) {
        if(child instanceof Void_Rune_Entity rune)rune.setCaster(boss.cast.damageOwner());
        return cloud(world,child,boss.cast,boss.amplification,boss.ability);
    }
    public static Explosion explosion(Entity source,Level world,double x,double y,double z,float radius) {
        var cast=NativeSpellDamage.context(source);
        return NativeSpellDamage.during(cast,()->world.explode(source,world.damageSources().explosion(source,cast.damageOwner()),new ExplosionDamageCalculator(){
            public boolean shouldDamageEntity(Explosion boom,Entity target){return !HarbingerCasting.allied(cast,target);}
            public float getKnockbackMultiplier(Entity target){return HarbingerCasting.allied(cast,target)?0:super.getKnockbackMultiplier(target);}
            public float getEntityDamageAmount(Explosion boom,Entity target){return Resources.augmentedDamage(super.getEntityDamageAmount(boom,target),NativeDamageContexts.amplification(source));}
        },x,y,z,radius,false,Level.ExplosionInteraction.NONE));
    }
    private GuardianCasting() {}
}
