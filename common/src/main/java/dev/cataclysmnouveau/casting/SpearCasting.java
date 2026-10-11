package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import java.util.*;

/** Accelerating native spears share the existing Core carrier/session contract. */
public final class SpearCasting {
    public static Elemental_Spear_Entity create(Ability ability,ServerLevel world,CastContext cast,Vec3 direction,double amp,double acceleration) {
        float damage=Math.max(0,CataclysmConfig.scyllaSpearDamage()+Resources.damageBonus(amp));
        Elemental_Spear_Entity spear;
        if(ability==Ability.WATER_SPEAR) {
            var water=new Water_Spear_Entity(cast.damageOwner(),direction,world,damage,2*Math.max(.1,1+acceleration/.75));
            water.setTotalBounces(8);spear=water;
        } else if(ability==Ability.LIGHTNING_SPEAR) {
            var lightning=new Lightning_Spear_Entity(cast.damageOwner(),direction,world,damage,2*Math.max(.1,1+acceleration/.75));
            lightning.setAreaDamage(Math.max(0,CataclysmConfig.scyllaFieldDamage()+Resources.damageBonus(amp)));
            lightning.setAreaRadius(2);lightning.setHpDamage(CataclysmConfig.scyllaHpDamage());spear=lightning;
        } else throw new IllegalArgumentException("Not a Scylla spear: "+ability);
        ((ScyllaPayload)spear).confluxScylla$cast(cast);
        return spear;
    }
    public static boolean replace(Ability ability,EntityProjectileSpell carrier,CarrierProfile profile) {
        if(!(carrier.level() instanceof ServerLevel world)) return false;
        var resolver=ArsSpellAccess.resolver(carrier);var cast=CastContext.of(resolver.spellContext,null,null);
        if(cast.caster()==null || !cast.caster().isAlive()) return true;
        var stats=resolver.getCastStats();var quote=Resources.quote(resolver.spellContext,ability,stats.getAugments());
        var spear=create(ability,world,cast,TriggerGeometry.direction(carrier),stats.getAmpMultiplier(),stats.getAccMultiplier());
        var driver=new Flight(spear,carrier,profile);var continuation=resolver.clone();
        var session=new ExecutionSession<>(world,cast.withAccount(quote.account()),spear,
                Math.max(1,Math.min(1200,carrier.getExpirationTime()+1)),driver,
                hit->{var child=continuation.clone();CastContexts.bind(child.spellContext,CastContexts.current().withAccount(cast.account()));child.onResolveEffect(spear.level(),hit);},
                ()->GlyphCatalog.projectile(ability).isEnabled(),ExecutionScope.DIRECT).billing(quote::trigger,profile.repeated());
        ExecutionSessions.start(session);return true;
    }
    private static final class Flight implements ExecutionDriver<Elemental_Spear_Entity> {
        private final Elemental_Spear_Entity spear;
        private final EntityProjectileSpell carrier;
        private final CarrierProfile profile;
        private final ArsTrajectory trajectory;
        private final Set<UUID> continued=new HashSet<>();
        private Vec3 direction;
        Flight(Elemental_Spear_Entity spear,EntityProjectileSpell carrier,CarrierProfile profile) {
            this.spear=spear;this.carrier=carrier;this.profile=profile;
            trajectory=profile.arsDriven()?new ArsTrajectory(spear,carrier):null;direction=TriggerGeometry.direction(carrier);
        }
        public Vec3 incomingDirection() {return direction;}
        public boolean start(ExecutionSession<Elemental_Spear_Entity> session) {
            var state=(ManagedProjectile)spear;state.confluxCataclysm$mode(trajectory==null?2:1);
            state.confluxCataclysm$carrier(carrier);state.confluxCataclysm$repeated(profile.repeated());
            state.confluxCataclysm$impact(hit->impact(session,hit));
            if(trajectory!=null) return trajectory.start(session.world(),()->{},hit->impact(session,hit));
            spear.setPos(carrier.position());
            spear.setYRot((float)Math.toDegrees(Math.atan2(direction.x,direction.z)));
            spear.setXRot((float)Math.toDegrees(Math.atan2(direction.y,direction.horizontalDistance())));
            boolean spawned=session.world().addFreshEntity(spear);if(spawned) session.trackProjectile(spear);return spawned;
        }
        private void impact(ExecutionSession<Elemental_Spear_Entity> session,HitResult hit) {
            var state=(ManagedProjectile)spear;
            if(hit.getType()==HitResult.Type.MISS || state.confluxCataclysm$resolving()) return;
            var cast=((ScyllaPayload)spear).confluxScylla$cast();
            if(hit instanceof EntityHitResult entity && (dev.arsconflux.api.targeting.Allies.areAllies(cast.caster(),entity.getEntity())
                    || dev.arsconflux.api.targeting.Allies.areAllies(cast.damageOwner(),entity.getEntity()))) return;
            direction=trajectory==null?spear.getDeltaMovement().normalize():trajectory.incomingDirection();
            state.confluxCataclysm$resolving(true);
            try {
                if(!session.activate(()->{
                    if(trajectory!=null) {spear.setPos(hit.getLocation());spear.setDeltaMovement(direction);}
                    ((ProjectileHitAccess)spear).confluxCataclysm$nativeHit(hit);
                    if(profile.repeated() || !(hit instanceof EntityHitResult entity) || continued.add(entity.getEntity().getUUID())) session.impact(hit);
                    return true;
                })) session.finish(ExecutionSession.EndReason.DENIED);
            } finally {state.confluxCataclysm$resolving(false);}
        }
        public boolean tick(ExecutionSession<Elemental_Spear_Entity> session) {
            return spear.isRemoved() || !session.world().hasChunkAt(spear.blockPosition()) || trajectory!=null && trajectory.tick(session.world());
        }
        public void close(ExecutionSession<Elemental_Spear_Entity> session,ExecutionSession.EndReason reason) {
            var state=(ManagedProjectile)spear;state.confluxCataclysm$impact(null);state.confluxCataclysm$carrier(null);state.confluxCataclysm$resolving(false);
            if(trajectory!=null) trajectory.close();else {spear.discard();carrier.discard();}
        }
    }
    private SpearCasting() {}
}
