package dev.cataclysmnouveau.casting;
import dev.arsconflux.api.damage.NativeSpellDamage;

import com.github.L_Ender.cataclysm.entity.effect.Wave_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Player_Ceraunus_Entity;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Native returning arrow and contact-volume wave have different hit/lifetime contracts. */
public final class NativeCarrierCasting {
    public static boolean replace(Ability ability,EntityProjectileSpell carrier,CarrierProfile profile) {
        if (!(carrier.level() instanceof ServerLevel world)) return false;
        var resolver=ArsSpellAccess.resolver(carrier);
        var frame=CastContext.of(resolver.spellContext,null,null);
        if (frame.caster()==null || !frame.caster().isAlive()) return true;
        var stats=resolver.getCastStats();var quote=Resources.quote(resolver.spellContext,ability,stats.getAugments());
        int duration=ability==Ability.WAVE?(profile.repeated()?Math.max(40,Math.min(1200,carrier.getExpirationTime()+1)):waveDuration(stats.getDurationMultiplier())):Math.max(40,Math.min(1200,carrier.getExpirationTime()+1));
        Entity payload;
        if (ability==Ability.CERAUNUS) {
            var anchor=new Player_Ceraunus_Entity(world,frame.damageOwner());
            anchor.setBaseDamage(Math.max(0,CataclysmConfig.anchorDamage()));
            payload=anchor;
        } else {
            var wave=new Wave_Entity(world,frame.damageOwner(),duration,Math.max(0,CataclysmConfig.waveDamage()));
            wave.setState(1);payload=wave;
        }
        NativeDamageContexts.bind(payload,frame,stats.getAmpMultiplier());
        var driver=new Flight(payload,carrier,profile.arsDriven(),profile.repeated(),stats.getAccMultiplier());
        var continuation=resolver.clone();
        var session=new ExecutionSession<>(world,frame.withAccount(quote.account()),payload,duration+2,driver,
                hit->{var child=continuation.clone();CastContexts.bind(child.spellContext,CastContexts.current().withAccount(frame.account()));child.onResolveEffect(payload.level(),hit);},
                ()->GlyphCatalog.projectile(ability).isEnabled(),ExecutionScope.DIRECT).billing(quote::trigger,profile.repeated());
        ExecutionSessions.start(session);return true;
    }
    public static int waveDuration(double durationModifier) {
        double scale=Double.isFinite(durationModifier)?Math.max(.1,1+.5*durationModifier):1;
        return (int)Math.max(40,Math.min(1200,Math.round(CataclysmConfig.waveLifetime()*scale)));
    }
    private static final class Flight implements ExecutionDriver<Entity> {
        private final Entity payload;
        private final EntityProjectileSpell carrier;
        private final ArsTrajectory trajectory;
        private final boolean repeated;
        private final float speed;
        private final Set<UUID> continuedEntities=new HashSet<>();
        private final Set<BlockPos> continuedBlocks=new HashSet<>();
        private boolean anchorHit;
        private Vec3 direction;
        Flight(Entity payload,EntityProjectileSpell carrier,boolean arsDriven,boolean repeated,double acceleration) {
            this.payload=payload;this.carrier=carrier;this.repeated=repeated;
            trajectory=arsDriven?new ArsTrajectory(payload,carrier):null;
            direction=TriggerGeometry.direction(carrier);speed=(float)Math.max(.1,1+acceleration/.75);
        }
        public Vec3 incomingDirection() { return direction; }
        public boolean start(ExecutionSession<Entity> session) {
            var state=(ManagedProjectile)payload;
            state.confluxCataclysm$mode(trajectory==null?2:1);state.confluxCataclysm$carrier(carrier);state.confluxCataclysm$repeated(repeated);
            state.confluxCataclysm$impact(hit->{if(payload instanceof Wave_Entity && trajectory==null) nativeContact(session,hit);else impact(session,hit);});
            if(payload instanceof Wave_Entity wave) {
                wave.setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
                ((WaveControl)wave).confluxCataclysm$speed(speed);
            }
            if(trajectory!=null) return trajectory.start(session.world(),()->{},hit->impact(session,hit));
            payload.setPos(carrier.position());
            if(payload instanceof Player_Ceraunus_Entity anchor) anchor.shoot(direction.x,direction.y,direction.z,2.5F*speed,0);
            boolean spawned=session.world().addFreshEntity(payload);
            if(spawned) session.trackProjectile(payload);
            return spawned;
        }
        private void impact(ExecutionSession<Entity> session,HitResult hit) {
            var state=(ManagedProjectile)payload;
            if(hit.getType()==HitResult.Type.MISS || state.confluxCataclysm$resolving() || anchorHit) return;
            direction=trajectory==null?payload.getDeltaMovement().normalize():trajectory.incomingDirection();
            state.confluxCataclysm$resolving(true);
            try {
                if(!session.activate(()->{
                    payload.setPos(hit.getLocation());
                    boolean continued=dev.arsconflux.api.projectile.ImpactTriggers.resolve(hit,
                            dev.arsconflux.api.projectile.ImpactTriggers.Policy.ENTITY_DAMAGE, () -> {
                    if(payload instanceof Player_Ceraunus_Entity anchor) {
                        ((ProjectileHitAccess)anchor).confluxCataclysm$nativeHit(hit);
                        if(hit instanceof BlockHitResult) session.world().broadcastEntityEvent(anchor,(byte)73);
                        if(!repeated) {anchorHit=true;state.confluxCataclysm$mode(2);anchor.setNoPhysics(true);}
                    } else {
                        var wave=(Wave_Entity)payload;
                        wave.setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
                        ((WaveControl)wave).confluxCataclysm$attack(1.5,-direction.x,-direction.z);
                    }
                    });
                    if(continued) session.impact(hit);return true;
                })) session.finish(ExecutionSession.EndReason.DENIED);
            } finally {state.confluxCataclysm$resolving(false);}
        }
        private void nativeContact(ExecutionSession<Entity> session,HitResult hit) {
            if(!session.active() || !session.permitted()) return;
            if(hit instanceof EntityHitResult entity && !continuedEntities.add(entity.getEntity().getUUID())) return;
            if(hit instanceof BlockHitResult block && !continuedBlocks.add(block.getBlockPos())) return;
            direction=payload.getDeltaMovement().lengthSqr()>1e-8?payload.getDeltaMovement().normalize():direction;
            session.impact(hit);
        }
        public boolean tick(ExecutionSession<Entity> session) {
            if(payload.isRemoved() || !session.world().hasChunkAt(payload.blockPosition())) return true;
            if(payload instanceof Player_Ceraunus_Entity && anchorHit) return false; // native return owns movement now
            if(trajectory!=null) {
                boolean ended=trajectory.tick(session.world());
                return payload instanceof Player_Ceraunus_Entity && anchorHit?payload.isRemoved():ended;
            }
            if(payload instanceof Wave_Entity wave && wave.horizontalCollision) {
                var from=wave.position().add(0,.5,0);
                var hit=session.world().clip(new ClipContext(from,from.add(direction.multiply(1,0,1).normalize().scale(1.0)),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,wave));
                if(hit.getType()==HitResult.Type.BLOCK) nativeContact(session,hit);
            }
            return false;
        }
        public void close(ExecutionSession<Entity> session,ExecutionSession.EndReason reason) {
            var state=(ManagedProjectile)payload;state.confluxCataclysm$impact(null);state.confluxCataclysm$carrier(null);
            state.confluxCataclysm$resolving(false);
            if(trajectory!=null) trajectory.close();else {payload.discard();carrier.discard();}
        }
    }
    private NativeCarrierCasting() {}
}
