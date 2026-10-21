package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.Axe_Blade_Entity;
import com.hollingsworth.arsnouveau.common.entity.EntityProjectileSpell;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.glyph.ArsSpellAccess;
import dev.arsconflux.api.projectile.*;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.AxeBladeAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.*;
import java.util.*;

/** Native non-Projectile entity with piercing contact damage and one Ars continuation per victim. */
public final class AxeBladeCasting {
    public static boolean replace(EntityProjectileSpell carrier,CarrierProfile profile){
        if(!(carrier.level() instanceof ServerLevel world))return false;
        var resolver=ArsSpellAccess.resolver(carrier);var cast=CastContext.of(resolver.spellContext,null,null);
        if(cast.caster()==null || !cast.caster().isAlive())return true;
        var stats=resolver.getCastStats();var quote=Resources.quote(resolver.spellContext,Ability.AXE_BLADE,stats.getAugments());
        var direction=TriggerGeometry.direction(carrier);var owner=cast.damageOwner()==null?cast.caster():cast.damageOwner();
        double speed=Math.max(.1,Math.min(20,1+stats.getAccMultiplier()/.75));
        var blade=new Axe_Blade_Entity(owner,direction.x,direction.y,direction.z,world,Math.max(0,(float)CMCommonConfig.Aptrgangr.AxeBladeDamage),yaw(direction));
        blade.xPower*=speed;blade.yPower*=speed;blade.zPower*=speed;NativeDamageContexts.bind(blade,cast,stats.getAmpMultiplier());
        var continuation=resolver.clone();var driver=new Flight(blade,carrier,profile,direction);
        ExecutionSessions.start(new ExecutionSession<>(world,cast.withAccount(quote.account()),blade,
            profile.arsDriven()?Math.max(2,Math.min(1200,carrier.getExpirationTime()+1)):83,driver,
            hit->{var next=continuation.clone();CastContexts.bind(next.spellContext,CastContexts.current().withAccount(cast.account()));next.onResolveEffect(blade.level(),hit);},
            ()->GlyphCatalog.AXE_BLADE.isEnabled(),ExecutionScope.DIRECT).billing(quote::trigger,profile.repeated()));
        return true;
    }
    private static float yaw(Vec3 d){return (float)Math.toDegrees(Math.atan2(-d.x,d.z));}
    private static final class Flight implements ExecutionDriver<Axe_Blade_Entity> {
        final Axe_Blade_Entity blade;final EntityProjectileSpell carrier;final ArsTrajectory trajectory;final boolean repeated;
        final Set<UUID> continued=new HashSet<>();Vec3 direction;
        Flight(Axe_Blade_Entity blade,EntityProjectileSpell carrier,CarrierProfile profile,Vec3 direction){
            this.blade=blade;this.carrier=carrier;this.direction=direction;repeated=profile.repeated();trajectory=profile.arsDriven()?new ArsTrajectory(blade,carrier):null;
        }
        public Vec3 incomingDirection(){return direction;}
        public boolean start(ExecutionSession<Axe_Blade_Entity> s){
            var state=(ManagedProjectile)blade;state.confluxCataclysm$mode(trajectory==null?2:1);state.confluxCataclysm$carrier(carrier);
            state.confluxCataclysm$sourceAbility(Ability.AXE_BLADE);state.confluxCataclysm$repeated(repeated);state.confluxCataclysm$impact(hit->impact(s,hit));
            if(trajectory!=null)return trajectory.start(s.world(),()->blade.setYRot(yaw(direction)),hit->impact(s,hit));
            blade.setPos(carrier.position());boolean added=s.world().addFreshEntity(blade);if(added)s.trackProjectile(blade);return added;
        }
        private void impact(ExecutionSession<Axe_Blade_Entity> s,HitResult hit){
            var state=(ManagedProjectile)blade;
            if(state.confluxCataclysm$resolving() || hit.getType()==HitResult.Type.MISS)return;
            if(hit instanceof EntityHitResult e && HarbingerCasting.allied(s.context(),e.getEntity()))return;
            direction=trajectory==null?blade.getDeltaMovement().lengthSqr()>1e-8?blade.getDeltaMovement().normalize():direction:trajectory.incomingDirection();
            state.confluxCataclysm$resolving(true);
            try{
                if(hit instanceof BlockHitResult){if(trajectory==null)((AxeBladeAccess)blade).confluxAptrgangr$hit(hit);return;}
                if(!s.activate(()->{
                    boolean success=ImpactTriggers.resolve(hit,ImpactTriggers.Policy.ENTITY_DAMAGE,()->((AxeBladeAccess)blade).confluxAptrgangr$hit(hit));
                    if(success && hit instanceof EntityHitResult e && (repeated || continued.add(e.getEntity().getUUID())))s.impact(hit);
                    return true;
                }))s.finish(ExecutionSession.EndReason.DENIED);
            }finally{state.confluxCataclysm$resolving(false);}
        }
        public boolean tick(ExecutionSession<Axe_Blade_Entity> s){
            if(blade.isRemoved() || !s.world().hasChunkAt(blade.blockPosition()) || !s.world().hasChunkAt(BlockPos.containing(blade.position().add(blade.getDeltaMovement()))))return true;
            boolean done=trajectory!=null && trajectory.tick(s.world());
            if(trajectory!=null)blade.setYRot(yaw(trajectory.incomingDirection()));return done;
        }
        public void close(ExecutionSession<Axe_Blade_Entity> s,ExecutionSession.EndReason reason){
            var state=(ManagedProjectile)blade;state.confluxCataclysm$impact(null);state.confluxCataclysm$carrier(null);state.confluxCataclysm$resolving(false);
            if(trajectory!=null)trajectory.close();else{blade.discard();carrier.discard();}
        }
    }
    private AxeBladeCasting(){}
}
