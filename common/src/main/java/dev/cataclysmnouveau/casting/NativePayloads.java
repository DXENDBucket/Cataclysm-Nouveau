package dev.cataclysmnouveau.casting;
import com.github.L_Ender.cataclysm.config.CMCommonConfig;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.entity.effect.Bolt_strike_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.*;
import dev.cataclysmnouveau.mixin.ProjectileHitAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.*;
public final class NativePayloads {
    public static ThrowableProjectile projectile(Ability ability,ServerLevel world,CastContext frame,double amplify) {
        ThrowableProjectile result=switch(ability) {
            case AMETHYST_CLUSTER -> new Amethyst_Cluster_Projectile_Entity(ModEntities.AMETHYST_CLUSTER_PROJECTILE.get(),world,frame.caster(),Math.max(0,(float)CMCommonConfig.AmethystCrab.AmethystClusterDamage+Resources.damageBonus(amplify)));
            case LIONFISH_SPIKE -> new Lionfish_Spike_Entity(world,frame.caster());
            case LAVA_BOMB -> new Lava_Bomb_Entity(ModEntities.LAVA_BOMB.get(),world,frame.caster());
            case FLARE_BOMB -> new Flare_Bomb_Entity(ModEntities.FLARE_BOMB.get(),world,frame.caster());
            case SPARK -> {
                var spark=new Spark_Entity(world,frame.damageOwner());
                spark.setDamage(Math.max(0,CataclysmConfig.scyllaStormDamage()+Resources.damageBonus(amplify)));
                spark.setAreaDamage(Math.max(0,CataclysmConfig.scyllaFieldDamage()+Resources.damageBonus(amplify)));
                spark.setAreaRadius(2);spark.setHpDamage(CataclysmConfig.scyllaHpDamage());
                yield ScyllaCasting.manage(spark,frame);
            }
            default -> throw new IllegalArgumentException("Not a projectile: "+ability);
        };
        result.setOwner(frame.damageOwner());
        ((ManagedProjectile)result).confluxCataclysm$bonus(Resources.damageBonus(amplify));
        return result;
    }
    public static void hit(ThrowableProjectile projectile,HitResult hit) {
        if (projectile instanceof Lava_Bomb_Entity lava && ((ManagedProjectile)lava).confluxCataclysm$mode() > 0
                && (((ManagedProjectile)lava).confluxCataclysm$resolving() || ((ManagedProjectile)lava).confluxCataclysm$impact()==null))
            LavaCasting.impact(lava, hit);
        else ((ProjectileHitAccess)projectile).confluxCataclysm$nativeHit(hit);
    }
    public static boolean bolt(ServerLevel world,CastContext frame,double amplify) {
        if (frame.target()==null || frame.target().getType()==HitResult.Type.MISS) return false;
        Vec3 point=frame.target() instanceof EntityHitResult entity?entity.getEntity().position():frame.origin();
        var bolt=new Bolt_strike_Entity(world,point.x,point.y,point.z,0,CataclysmConfig.boltWarmup(),Math.max(0,CataclysmConfig.boltDamage()+Resources.damageBonus(amplify)),frame.damageOwner());
        bolt.setR(80);bolt.setG(170);bolt.setB(255);
        return world.addFreshEntity(bolt);
    }

    public static boolean effect(Ability ability, ServerLevel world, CastContext frame, double amplify) {
        if (ability == Ability.BOLT_STRIKE) return bolt(world, frame, amplify);
        Vec3 point = groundPoint(world, frame);
        if (point == null) return false;
        float yaw = frame.damageOwner() == null ? 0 : (float)Math.toRadians(frame.damageOwner().getYRot());
        float damage = Math.max(0, CataclysmConfig.groundDamage(ability) + Resources.damageBonus(amplify));
        var entity = switch (ability) {
            case VOID_RUNE -> new Void_Rune_Entity(world, point.x, point.y, point.z, yaw,
                    CataclysmConfig.groundWarmup(ability), damage, frame.damageOwner());
            case FLAME_JET -> new Flame_Jet_Entity(world, point.x, point.y, point.z, yaw,
                    CataclysmConfig.groundWarmup(ability), damage, frame.damageOwner());
            default -> throw new IllegalArgumentException("Not a ground effect: " + ability);
        };
        return world.addFreshEntity(entity);
    }

    /** Local ground projection only: never scan a cave column or cause chunk loading. */
    public static Vec3 groundPoint(ServerLevel world, CastContext frame) {
        if (frame.target() == null || frame.target().getType() == HitResult.Type.MISS) return null;
        Vec3 point = frame.target() instanceof EntityHitResult hit ? hit.getEntity().position() : frame.origin();
        BlockPos start = BlockPos.containing(point.add(0, 0.01, 0));
        for (int offset = 0; offset <= 4; offset++) {
            BlockPos pos = start.below(offset);
            if (!world.isInWorldBounds(pos) || !world.hasChunkAt(pos)) return null;
            var shape = world.getBlockState(pos).getCollisionShape(world, pos);
            if (shape.isEmpty()) continue;
            double x = point.x - pos.getX(), z = point.z - pos.getZ();
            double top = shape.toAabbs().stream()
                    .filter(box -> x >= box.minX && x <= box.maxX && z >= box.minZ && z <= box.maxZ)
                    .mapToDouble(box -> box.maxY).max().orElse(Double.NaN);
            if (Double.isNaN(top)) continue;
            double y = pos.getY() + top;
            // A ceiling's underside is not an upward-facing casting surface.
            if (frame.target() instanceof BlockHitResult hit && hit.getDirection() == Direction.DOWN
                    && offset == 0) continue;
            return new Vec3(point.x, y, point.z);
        }
        return null;
    }
    private NativePayloads() {}
}
