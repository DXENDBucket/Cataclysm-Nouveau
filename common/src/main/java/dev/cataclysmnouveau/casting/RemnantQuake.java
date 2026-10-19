package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.EarthQuake_Entity;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.damage.NativeSpellDamage;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.RemnantQuakeAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;
import java.util.*;
import java.util.function.BiFunction;

/** Contact wave: native movement damages grounded targets; special Ars forms own movement only. */
public final class RemnantQuake {
    public static boolean damage(EarthQuake_Entity wave,LivingEntity target,DamageSource source,float damage,BiFunction<DamageSource,Float,Boolean> original) {
        var state=(ManagedProjectile)wave;
        if(state.confluxCataclysm$mode()==1 && !state.confluxCataclysm$resolving())return false;
        var cast=wave.level().isClientSide?null:NativeDamageContexts.context(wave);
        if(cast!=null && HarbingerCasting.allied(cast,target))return false;
        if(cast!=null)damage=Resources.augmentedDamage(damage,NativeDamageContexts.amplification(wave));
        boolean hit=NativeSpellDamage.hurt(cast,target,source,damage,(s,a)->original.apply(s,a));
        if(hit && !wave.level().isClientSide && state.confluxCataclysm$mode()==2 && state.confluxCataclysm$impact()!=null)
            state.confluxCataclysm$impact().accept(new EntityHitResult(target));
        return hit;
    }
    public static boolean replace(EntityProjectileSpell carrier,CarrierProfile profile) {
        if(!(carrier.level() instanceof ServerLevel world))return false;
        var resolver=ArsSpellAccess.resolver(carrier);var cast=CastContext.of(resolver.spellContext,null,null);
        if(cast.caster()==null || !cast.caster().isAlive())return true;
        var stats=resolver.getCastStats();var quote=Resources.quote(resolver.spellContext,Ability.GROUND_QUAKE,stats.getAugments());
        var wave=new EarthQuake_Entity(world,cast.damageOwner());wave.setDamage(Math.max(0,(float)CMCommonConfig.AncientRemnant.EarthQuakeDamage));
        NativeDamageContexts.bind(wave,cast,stats.getAmpMultiplier());
        var continuation=resolver.clone();var flight=new Flight(wave,carrier,profile,stats.getAccMultiplier());
        ExecutionSessions.start(new ExecutionSession<>(world,cast.withAccount(quote.account()),wave,
            profile.arsDriven()?Math.max(2,Math.min(1200,carrier.getExpirationTime()+1)):65,flight,
            hit->{var next=continuation.clone();CastContexts.bind(next.spellContext,CastContexts.current().withAccount(cast.account()));next.onResolveEffect(wave.level(),hit);},
            ()->GlyphCatalog.projectile(Ability.GROUND_QUAKE).isEnabled(),ExecutionScope.DIRECT).billing(quote::trigger,profile.repeated()));
        return true;
    }
    private static final class Flight implements ExecutionDriver<EarthQuake_Entity> {
        final EarthQuake_Entity wave;final EntityProjectileSpell carrier;final ArsTrajectory trajectory;final boolean repeated;final float speed;
        final Set<UUID> continued=new HashSet<>();Vec3 direction;
        Flight(EarthQuake_Entity wave,EntityProjectileSpell carrier,CarrierProfile profile,double acceleration){
            this.wave=wave;this.carrier=carrier;repeated=profile.repeated();trajectory=profile.arsDriven()?new ArsTrajectory(wave,carrier):null;
            speed=(float)(.45*Math.max(.1,1+acceleration/.75));direction=TriggerGeometry.direction(carrier);
        }
        public Vec3 incomingDirection(){return direction;}
        public boolean start(ExecutionSession<EarthQuake_Entity> s){
            var state=(ManagedProjectile)wave;state.confluxCataclysm$mode(trajectory==null?2:1);state.confluxCataclysm$sourceAbility(Ability.GROUND_QUAKE);
            state.confluxCataclysm$carrier(carrier);state.confluxCataclysm$repeated(repeated);
            state.confluxCataclysm$impact(hit->{if(trajectory==null)contact(s,hit);else impact(s,hit);});
            if(trajectory!=null)return trajectory.start(s.world(),()->{},hit->impact(s,hit));
            wave.setPos(carrier.position());var horizontal=direction.multiply(1,0,1).normalize();
            if(horizontal.lengthSqr()<1e-8)horizontal=new Vec3(0,0,1);
            wave.shoot(horizontal.x,-1,horizontal.z,speed,0);return s.world().addFreshEntity(wave);
        }
        private void contact(ExecutionSession<EarthQuake_Entity> s,HitResult hit){
            if(!s.active() || !s.permitted() || !(hit instanceof EntityHitResult e) || !continued.add(e.getEntity().getUUID()))return;
            if(wave.getDeltaMovement().lengthSqr()>1e-8)direction=wave.getDeltaMovement().normalize();s.impact(hit);
        }
        private void impact(ExecutionSession<EarthQuake_Entity> s,HitResult hit){
            var state=(ManagedProjectile)wave;
            if(hit.getType()==HitResult.Type.MISS || state.confluxCataclysm$resolving())return;
            if(hit instanceof EntityHitResult e && (!(e.getEntity() instanceof LivingEntity living) || !living.onGround() || HarbingerCasting.allied(s.context(),living)))return;
            direction=trajectory.incomingDirection();state.confluxCataclysm$resolving(true);
            try{
                if(!s.activate(()->{
                    wave.setPos(hit.getLocation());
                    boolean success=ImpactTriggers.resolve(hit,ImpactTriggers.Policy.ENTITY_DAMAGE,()->{
                        int old=wave.tickCount;wave.tickCount=0;
                        try{((RemnantQuakeAccess)wave).confluxRemnant$contact();}finally{wave.tickCount=old;}
                    });
                    if(success)s.impact(hit);return true;
                }))s.finish(ExecutionSession.EndReason.DENIED);
            }finally{state.confluxCataclysm$resolving(false);}
        }
        public boolean tick(ExecutionSession<EarthQuake_Entity> s){
            if(wave.isRemoved() || !s.world().hasChunkAt(wave.blockPosition()) || !s.world().hasChunkAt(net.minecraft.core.BlockPos.containing(wave.position().add(wave.getDeltaMovement()))))return true;
            return trajectory!=null && trajectory.tick(s.world());
        }
        public void close(ExecutionSession<EarthQuake_Entity> s,ExecutionSession.EndReason reason){
            var state=(ManagedProjectile)wave;state.confluxCataclysm$impact(null);state.confluxCataclysm$carrier(null);state.confluxCataclysm$resolving(false);
            if(trajectory!=null)trajectory.close();else{wave.discard();carrier.discard();}
        }
    }
    private RemnantQuake(){}
}
