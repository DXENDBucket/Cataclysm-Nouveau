package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.*;

/** Physical origin of an action, independent of its payer and damage owner. */
public record ActionAnchor(LivingEntity entity, Vec3 point, Vec3 initialDirection) {
    /** A beneficial action may act at a position, but cannot commandeer an enemy entity. */
    public static ActionAnchor beneficial(CastContext cast) {
        if (cast.target() instanceof EntityHitResult hit) {
            if (!(hit.getEntity() instanceof LivingEntity actor) || !HarbingerCasting.allied(cast,actor)) return null;
            return new ActionAnchor(actor,null,horizontal(actor.getLookAngle(),new Vec3(0,0,1)));
        }
        if (cast.target() instanceof BlockHitResult hit && hit.getType()==HitResult.Type.BLOCK) {
            Vec3 incoming=cast.incomingDirection();
            return new ActionAnchor(null,hit.getLocation(),horizontal(incoming,cast.caster().getLookAngle()));
        }
        return null;
    }
    public Vec3 position() { return entity==null?point:entity.position(); }
    public AABB bounds() {
        return entity==null?new AABB(point.x-.3,point.y,point.z-.3,point.x+.3,point.y+1.8,point.z+.3):entity.getBoundingBox();
    }
    public boolean present(ServerLevel world,int radius) {
        return (entity==null || entity.isAlive() && !entity.isRemoved() && entity.level()==world && !entity.isPassenger())
                && Double.isFinite(position().lengthSqr()) && MonstrosityCasting.loadedArea(world,position(),radius);
    }
    public static Vec3 horizontal(Vec3 direction,Vec3 fallback) {
        if(direction!=null && Double.isFinite(direction.lengthSqr())) {
            Vec3 flat=direction.multiply(1,0,1);
            if(flat.lengthSqr()>1e-8)return flat.normalize();
        }
        if(fallback!=null && Double.isFinite(fallback.lengthSqr())) {
            Vec3 flat=fallback.multiply(1,0,1);
            if(flat.lengthSqr()>1e-8)return flat.normalize();
        }
        return new Vec3(0,0,1);
    }
}
