package dev.cataclysmnouveau.casting;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.*;
public final class ProjectileCasting {
    public static boolean replace(Ability ability,EntityProjectileSpell carrier,CarrierProfile profile) {
        if (ability==Ability.CERAUNUS || ability==Ability.WAVE)
            return NativeCarrierCasting.replace(ability,carrier,profile);
        if (!(carrier.level() instanceof ServerLevel world)) return false;
        var resolver=ArsSpellAccess.resolver(carrier);
        var frame=CastContext.of(resolver.spellContext,null,null);
        if (frame.caster()==null || !frame.caster().isAlive()) return true;
        var stats=resolver.getCastStats();var quote=Resources.quote(resolver.spellContext,ability,stats.getAugments());
        var payload=NativePayloads.projectile(ability,world,frame,stats.getAmpMultiplier());
        var driver=new Flight(ability,payload,carrier,profile.arsDriven(),profile.repeated(),stats.getAccMultiplier());
        var continuation=resolver.clone();
        var session=new ExecutionSession<>(world,frame.withAccount(quote.account()),payload,
            Math.max(1,Math.min(1200,carrier.getExpirationTime()+1)),driver,
            hit->{
                var child=continuation.clone();
                CastContexts.bind(child.spellContext,CastContexts.current().withAccount(frame.account()));
                child.onResolveEffect(payload.level(),hit);
            },()->GlyphCatalog.projectile(ability).isEnabled(),ExecutionScope.DIRECT).billing(quote::trigger,profile.repeated());
        ExecutionSessions.start(session);return true;
    }
    private static final class Flight implements ExecutionDriver<ThrowableProjectile> {
        private final Ability ability;
        private final ThrowableProjectile payload;
        private final EntityProjectileSpell carrier;
        private final ArsTrajectory trajectory;
        private final boolean repeated;
        private final float acceleration;
        private Vec3 direction;
        Flight(Ability ability,ThrowableProjectile payload,EntityProjectileSpell carrier,boolean arsDriven,boolean repeated,double acceleration) {
            this.ability=ability;this.payload=payload;this.carrier=carrier;this.repeated=repeated;this.acceleration=(float)acceleration;
            trajectory=arsDriven?new ArsTrajectory(payload,carrier):null;direction=TriggerGeometry.direction(carrier);
        }
        public Vec3 incomingDirection() { return direction; }
        public boolean start(ExecutionSession<ThrowableProjectile> session) {
            var state=(ManagedProjectile)payload;
            state.confluxCataclysm$mode(trajectory==null?2:1);state.confluxCataclysm$carrier(carrier);state.confluxCataclysm$repeated(repeated);
            state.confluxCataclysm$impact(hit->impact(session,hit));
            if (trajectory!=null) return trajectory.start(session.world(),()->{},hit->impact(session,hit));
            payload.setPos(carrier.position());
            float speed=(ability==Ability.AMETHYST_CLUSTER?.8F:(ability==Ability.FLARE_BOMB || ability==Ability.LAVA_BOMB)?1F:.35F)*Math.max(.1F,1+acceleration/.75F);
            payload.shoot(direction.x,direction.y,direction.z,speed,0);
            return session.world().addFreshEntity(payload);
        }
        private void impact(ExecutionSession<ThrowableProjectile> session,HitResult hit) {
            var state=(ManagedProjectile)payload;
            if (hit.getType()==HitResult.Type.MISS || state.confluxCataclysm$resolving()) return;
            direction=trajectory==null?payload.getDeltaMovement().normalize():trajectory.incomingDirection();
            state.confluxCataclysm$resolving(true);
            try {
                if (!session.activate(()->{
                    payload.setPos(hit.getLocation());NativePayloads.hit(payload,hit);session.impact(hit);return true;
                })) session.finish(ExecutionSession.EndReason.DENIED);
            } finally { state.confluxCataclysm$resolving(false); }
        }
        public boolean tick(ExecutionSession<ThrowableProjectile> session) { return trajectory==null?payload.isRemoved():trajectory.tick(session.world()); }
        public void close(ExecutionSession<ThrowableProjectile> session,ExecutionSession.EndReason reason) {
            var state=(ManagedProjectile)payload;state.confluxCataclysm$carrier(null);state.confluxCataclysm$impact(null);
            state.confluxCataclysm$resolving(false);
            if (trajectory!=null) trajectory.close();else { payload.discard();carrier.discard(); }
        }
    }
    private ProjectileCasting() {}
}
