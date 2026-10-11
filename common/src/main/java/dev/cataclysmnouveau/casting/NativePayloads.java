package dev.cataclysmnouveau.casting;
import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.Bolt_strike_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.*;
public final class NativePayloads {
    public static ThrowableProjectile projectile(Ability ability,ServerLevel world,CastContext frame,double amplify) {
        ThrowableProjectile result=switch(ability) {
            case AMETHYST_CLUSTER -> new Amethyst_Cluster_Projectile_Entity(ModEntities.AMETHYST_CLUSTER_PROJECTILE.get(),world,frame.caster(),Math.max(0,(float)CMCommonConfig.AmethystCrab.AmethystClusterDamage+Resources.damageBonus(amplify)));
            case LIONFISH_SPIKE -> new Lionfish_Spike_Entity(world,frame.caster());
            default -> throw new IllegalArgumentException("Not a projectile: "+ability);
        };
        result.setOwner(frame.damageOwner());
        ((ManagedProjectile)result).confluxCataclysm$bonus(Resources.damageBonus(amplify));
        return result;
    }
    public static void hit(ThrowableProjectile projectile,HitResult hit) {
        ((ProjectileHitAccess)projectile).confluxCataclysm$nativeHit(hit);
    }
    public static boolean bolt(ServerLevel world,CastContext frame,double amplify) {
        if (frame.target()==null || frame.target().getType()==HitResult.Type.MISS) return false;
        Vec3 point=frame.target() instanceof EntityHitResult entity?entity.getEntity().position():frame.origin();
        var bolt=new Bolt_strike_Entity(world,point.x,point.y,point.z,0,CataclysmConfig.boltWarmup(),Math.max(0,CataclysmConfig.boltDamage()+Resources.damageBonus(amplify)),frame.damageOwner());
        bolt.setR(80);bolt.setG(170);bolt.setB(255);
        return world.addFreshEntity(bolt);
    }
    private NativePayloads() {}
}
