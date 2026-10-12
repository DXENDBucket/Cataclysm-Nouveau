package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.Wither_Smoke_Effect_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.*;
import java.util.function.Supplier;

/** Native payloads own visuals/hit bodies; a single trajectory owns motion and collision. */
public final class HarbingerCasting {
    private static final ThreadLocal<Entity> DECORATING=new ThreadLocal<>();
    public static boolean supports(Ability a) {return switch(a){case LASER_BOLT,WITHER_MISSILE,HOMING_MISSILE,WITHER_HOWITZER->true;default->false;};}
    public static boolean nativePayload(Entity e) {return e instanceof Laser_Beam_Entity || e instanceof Wither_Missile_Entity || e instanceof Wither_Homing_Missile_Entity || e instanceof Wither_Howitzer_Entity;}
    public static boolean decorating(Entity e) {return DECORATING.get()==e && e instanceof ManagedProjectile s && s.confluxCataclysm$mode()==1;}
    public static void decorate(Entity e,Runnable tick) {
        var old=DECORATING.get();DECORATING.set(e);
        try{tick.run();}finally{if(old==null)DECORATING.remove();else DECORATING.set(old);}
    }
    public static boolean allied(CastContext c,Entity target) {
        return Allies.areAllies(c.caster(),target) || Allies.areAllies(c.damageOwner(),target) || Allies.areAllies(c.executor(),target);
    }
    public static boolean rejectDamage(Entity source,Entity target) {
        if(!nativePayload(source) && !(source instanceof Death_Laser_Beam_Entity) && !(source instanceof Wither_Smoke_Effect_Entity))return false;
        var c=NativeSpellDamage.context(source);if(c==null)return false;
        return allied(c,target) || source instanceof Death_Laser_Beam_Entity beam && beam.tickCount>20+beam.getDuration();
    }
    public static float damageAmount(Entity source,float amount) {
        if(source instanceof Wither_Howitzer_Entity && NativeSpellDamage.context(source)!=null)return Math.max(0,amount+((ManagedProjectile)source).confluxCataclysm$bonus());
        if(source instanceof HarbingerSmoke smoke && NativeSpellDamage.context(source)!=null)return Math.max(0,amount+smoke.confluxHarbinger$bonus());
        return amount;
    }
    public static LivingEntity target(ServerLevel world,Vec3 origin,Vec3 facing,CastContext c) {
        Vec3 end=origin;
        for(int i=1;i<=CataclysmConfig.harbingerRange();i++) {
            var next=origin.add(facing.scale(i));if(!world.hasChunkAt(BlockPos.containing(next)))break;end=next;
        }
        end=world.clip(new ClipContext(origin,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,c.caster())).getLocation();
        LivingEntity best=null;double distance=origin.distanceToSqr(end)+.01;
        for(var e:world.getEntitiesOfClass(LivingEntity.class,new AABB(origin,end).inflate(1))) {
            if(!e.isAlive() || e.isSpectator() || allied(c,e))continue;
            var point=e.getBoundingBox().inflate(.4).clip(origin,end);
            if(point.isPresent() && point.get().distanceToSqr(origin)<distance){best=e;distance=point.get().distanceToSqr(origin);}
        }
        return best;
    }
    public static Projectile create(Ability a,ServerLevel world,CastContext c,Vec3 direction,LivingEntity target,double amplify) {
        var owner=c.damageOwner()==null?c.caster():c.damageOwner();float bonus=Resources.damageBonus(amplify);
        Projectile p=switch(a) {
            case LASER_BOLT->new Laser_Beam_Entity(owner,direction,world,Math.max(0,(float)CMCommonConfig.Harbinger.Laserdamage+bonus));
            case WITHER_MISSILE->new Wither_Missile_Entity(owner,direction,world,Math.max(0,(float)CMCommonConfig.Harbinger.WitherMissiledamage+bonus));
            case HOMING_MISSILE->new Wither_Homing_Missile_Entity(owner,direction,world,Math.max(0,(float)CMCommonConfig.Harbinger.WitherMissiledamage+bonus),target);
            case WITHER_HOWITZER->{var shell=new Wither_Howitzer_Entity(ModEntities.WITHER_HOWITZER.get(),world,owner);shell.setRadius(3);yield shell;}
            default->throw new IllegalArgumentException("Not a Harbinger payload: "+a);
        };
        ((ManagedProjectile)p).confluxCataclysm$bonus(bonus);return NativeSpellDamage.bind(p,c);
    }
    public static boolean replace(Ability a,EntityProjectileSpell carrier,CarrierProfile profile) {
        if(!(carrier.level() instanceof ServerLevel world))return false;
        var resolver=ArsSpellAccess.resolver(carrier);var frame=CastContext.of(resolver.spellContext,null,null);
        if(frame.caster()==null || !frame.caster().isAlive())return true;
        Vec3 direction=TriggerGeometry.direction(carrier);
        var selected=a==Ability.HOMING_MISSILE?target(world,carrier.position(),direction,frame):null;
        if(a==Ability.HOMING_MISSILE && !profile.arsDriven() && selected==null)return true;
        var stats=resolver.getCastStats();var quote=Resources.quote(resolver.spellContext,a,stats.getAugments());
        var payload=create(a,world,frame,direction,selected,stats.getAmpMultiplier());
        var driver=new Flight(payload,carrier,profile,stats.getAccMultiplier());var continuation=resolver.clone();
        var session=new ExecutionSession<>(world,frame.withAccount(quote.account()),payload,
                Math.max(1,Math.min(1200,carrier.getExpirationTime()+1)),driver,
                hit->{var child=continuation.clone();CastContexts.bind(child.spellContext,CastContexts.current().withAccount(frame.account()));child.onResolveEffect(payload.level(),hit);},
                ()->GlyphCatalog.projectile(a).isEnabled(),ExecutionScope.DIRECT).billing(quote::trigger,profile.repeated());
        ExecutionSessions.start(session);return true;
    }
    private static final class Flight implements ExecutionDriver<Projectile> {
        final Projectile payload;final EntityProjectileSpell carrier;final ArsTrajectory trajectory;final boolean repeated;final double acceleration;
        Vec3 direction;
        Flight(Projectile p,EntityProjectileSpell carrier,CarrierProfile profile,double acc){payload=p;this.carrier=carrier;repeated=profile.repeated();acceleration=Math.max(.1,Math.min(20,1+acc/.75));trajectory=profile.arsDriven()?new ArsTrajectory(p,carrier):null;direction=TriggerGeometry.direction(carrier);}
        public Vec3 incomingDirection(){return direction;}
        public boolean start(ExecutionSession<Projectile> s) {
            if(payload instanceof Laser_Beam_Entity)s.world().playSound(null,carrier.blockPosition(),com.github.L_Ender.cataclysm.init.ModSounds.HARBINGER_LASER.get(),net.minecraft.sounds.SoundSource.PLAYERS,1,1);
            var state=(ManagedProjectile)payload;state.confluxCataclysm$mode(trajectory==null?2:1);state.confluxCataclysm$carrier(carrier);state.confluxCataclysm$repeated(repeated);state.confluxCataclysm$impact(h->impact(s,h));
            if(trajectory!=null)return trajectory.start(s.world(),()->{},h->impact(s,h));
            payload.setPos(carrier.position());
            if(payload instanceof Wither_Howitzer_Entity shell)shell.shoot(direction.x,direction.y,direction.z,(float)(.6*acceleration),0);
            else {payload.setDeltaMovement(payload.getDeltaMovement().scale(acceleration));if(payload instanceof Laser_Beam_Entity laser)laser.accelerationPower*=acceleration;if(payload instanceof Wither_Missile_Entity missile)missile.accelerationPower*=acceleration;if(payload instanceof Wither_Homing_Missile_Entity homing)homing.accelerationPower*=acceleration;}
            payload.setYRot((float)(Math.atan2(direction.x,direction.z)*180/Math.PI));payload.setXRot((float)(Math.atan2(direction.y,direction.horizontalDistance())*180/Math.PI));
            return s.world().addFreshEntity(payload);
        }
        void impact(ExecutionSession<Projectile> s,HitResult hit) {
            var state=(ManagedProjectile)payload;if(state.confluxCataclysm$resolving() || hit.getType()==HitResult.Type.MISS)return;
            if(hit instanceof EntityHitResult e && allied(s.context(),e.getEntity()))return;
            direction=trajectory==null?payload.getDeltaMovement().normalize():trajectory.incomingDirection();state.confluxCataclysm$resolving(true);
            try{if(!s.activate(()->{payload.setPos(hit.getLocation());((ProjectileHitAccess)payload).confluxCataclysm$nativeHit(hit);s.impact(hit);return true;}))s.finish(ExecutionSession.EndReason.DENIED);}
            finally{state.confluxCataclysm$resolving(false);}
        }
        public boolean tick(ExecutionSession<Projectile> s){return trajectory==null?payload.isRemoved():trajectory.tick(s.world());}
        public void close(ExecutionSession<Projectile> s,ExecutionSession.EndReason reason){var state=(ManagedProjectile)payload;state.confluxCataclysm$impact(null);state.confluxCataclysm$carrier(null);state.confluxCataclysm$resolving(false);if(trajectory!=null)trajectory.close();else{payload.discard();carrier.discard();}}
    }
    public static Explosion explosion(Entity source,Level level,double x,double y,double z,float radius) {
        var frame=NativeSpellDamage.context(source);float bonus=((ManagedProjectile)source).confluxCataclysm$bonus();
        return NativeSpellDamage.during(frame,()->level.explode(source,null,new ExplosionDamageCalculator(){
            public boolean shouldDamageEntity(Explosion boom,Entity target){return !allied(frame,target);}
            public float getKnockbackMultiplier(Entity target){return allied(frame,target)?0:super.getKnockbackMultiplier(target);}
            public float getEntityDamageAmount(Explosion boom,Entity target){return Math.max(0,super.getEntityDamageAmount(boom,target)+bonus);}
        },x,y,z,radius,false,Level.ExplosionInteraction.NONE));
    }
    public static boolean smoke(ServerLevel world,Entity child,Entity parent,Supplier<Boolean> original) {
        var frame=NativeSpellDamage.context(parent);if(frame==null || !(child instanceof Wither_Smoke_Effect_Entity cloud))return original.get();
        ((HarbingerSmoke)cloud).confluxHarbinger$managedSmoke(true);
        ((HarbingerSmoke)cloud).confluxHarbinger$bonus(((ManagedProjectile)parent).confluxCataclysm$bonus());NativeSpellDamage.bind(cloud,frame);
        return ExecutionSessions.start(new ExecutionSession<>(world,frame,cloud,cloud.getDuration()+30,new ExecutionDriver<Wither_Smoke_Effect_Entity>(){
            public boolean start(ExecutionSession<Wither_Smoke_Effect_Entity> s){return original.get();}
            public boolean tick(ExecutionSession<Wither_Smoke_Effect_Entity> s){return cloud.isRemoved() || !s.world().hasChunkAt(cloud.blockPosition());}
            public void close(ExecutionSession<Wither_Smoke_Effect_Entity> s,ExecutionSession.EndReason reason){cloud.discard();}
        },h->{},()->GlyphCatalog.HOWITZER.isEnabled(),ExecutionScope.DIRECT));
    }
    private HarbingerCasting() {}
}
