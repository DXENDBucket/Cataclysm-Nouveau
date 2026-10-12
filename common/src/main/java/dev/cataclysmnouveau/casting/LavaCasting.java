package dev.cataclysmnouveau.casting;
import dev.arsconflux.api.damage.NativeSpellDamage;

import com.github.L_Ender.cataclysm.entity.projectile.Lava_Bomb_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.*;
import dev.arsconflux.api.execution.*;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.*;
import net.minecraft.world.phys.HitResult;

public final class LavaCasting {
    public static void impact(Lava_Bomb_Entity carrier,HitResult hit) {
        if(!(carrier.level() instanceof ServerLevel world) || !(carrier.getOwner() instanceof LivingEntity owner)) return;
        var state=(ManagedProjectile)carrier;
        var frame=CastContexts.current();if(frame==null) frame=CastContext.detached(owner,hit,null);
        var pool=NativeDamageContexts.bind(new Lava_Bomb_Entity(ModEntities.LAVA_BOMB.get(),world,owner),frame,state.confluxCataclysm$amplification());
        pool.setPos(hit.getLocation());pool.maxLavaTime=CataclysmConfig.lavaLifetime();
        var poolState=(ManagedProjectile)pool;poolState.confluxCataclysm$mode(2);poolState.confluxCataclysm$amplification(state.confluxCataclysm$amplification());
        // A separate grounded native bomb owns each pool, including orbit/repeated impacts.
        var session=new ExecutionSession<>(world,frame,pool,pool.maxLavaTime+2,new ExecutionDriver<Lava_Bomb_Entity>() {
            public boolean start(ExecutionSession<Lava_Bomb_Entity> s) {
                if(!world.addFreshEntity(pool)) return false;
                ((ProjectileHitAccess)pool).confluxCataclysm$nativeHit(hit);
                return pool.getGround();
            }
            public boolean tick(ExecutionSession<Lava_Bomb_Entity> s) {return pool.isRemoved() || !world.hasChunkAt(pool.blockPosition());}
            public void close(ExecutionSession<Lava_Bomb_Entity> s,ExecutionSession.EndReason reason) {pool.discard();TemporaryLava.release(pool);}
        },h->{},()->GlyphCatalog.LAVA.isEnabled(),ExecutionScope.DIRECT);
        ExecutionSessions.start(session);
        carrier.discard(); // repeated Ars carriers suppress this through the existing opt-in discard hook
    }
    public static Explosion explosion(Lava_Bomb_Entity bomb,Level level,double x,double y,double z,float radius) {
        var owner=bomb.getOwner();float bonus=((ManagedProjectile)bomb).confluxCataclysm$amplification();
        return NativeSpellDamage.during(NativeDamageContexts.context(bomb),()->level.explode(owner,null,new ExplosionDamageCalculator() {
            @Override public boolean shouldDamageEntity(Explosion explosion,Entity target) {return !Allies.areAllies(owner,target);}
            @Override public float getKnockbackMultiplier(Entity target) {return Allies.areAllies(owner,target)?0:super.getKnockbackMultiplier(target);}
            @Override public float getEntityDamageAmount(Explosion explosion,Entity target) {return Resources.augmentedDamage(super.getEntityDamageAmount(explosion,target),NativeDamageContexts.amplification(bomb));}
        },x,y,z,radius,false,Level.ExplosionInteraction.NONE));
    }
    private LavaCasting() {}
}
