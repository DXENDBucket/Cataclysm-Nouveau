package dev.cataclysmnouveau.casting;

import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.ExecutionAnchor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.*;

/** Shared body-action aiming; Core owns anchor identity and authorization. */
final class ActionGeometry {
    final ExecutionAnchor anchor;
    final LivingEntity actor;
    final Vec3 initialFacing;
    private final ServerLevel world;
    private final CastContext context;
    private LivingEntity enemy;

    ActionGeometry(ServerLevel world,CastContext cast,ExecutionAnchor anchor) {
        this.world=world;this.anchor=anchor;actor=anchor.entity();context=cast.withExecutor(actor);
        initialFacing=horizontal(anchor.sample().direction(),yawDirection(actor==null?cast.caster():actor));
    }
    static ActionGeometry entity(ServerLevel world,CastContext cast,int area) {
        var anchor=ExecutionAnchor.beneficial(cast,ExecutionAnchor.Basis.FEET,Vec3.ZERO);
        if(anchor==null || anchor.entity()==null)return null;
        var geometry=new ActionGeometry(world,cast,anchor);
        return geometry.present(cast,area)?geometry:null;
    }
    Vec3 steer(Vec3 current,boolean permitted) {
        return permitted?turn(current,desired(current)):current;
    }
    /** Preserve the actual executor's vertical aim while sharing bounded horizontal steering. */
    Vec3 direction(Vec3 heading) {
        Vec3 look=actor==null?anchor.sample().direction():ExecutionAnchor.viewDirection(actor);
        if(actor instanceof Mob && validEnemy(enemy))look=enemy.getEyePosition().subtract(actor.getEyePosition()).normalize();
        return new Vec3(heading.x*look.horizontalDistance(),look.y,heading.z*look.horizontalDistance()).normalize();
    }
    void pose(LivingEntity frame,Vec3 heading) {
        frame.setPos(point());frame.setYRot((float)Math.toDegrees(Math.atan2(-heading.x,heading.z)));
        frame.yRotO=frame.yBodyRot=frame.yHeadRot=frame.getYRot();frame.setBoundingBox(bounds());
    }
    boolean present(CastContext cast,int area) {
        return anchor.valid(world,cast) && (actor==null || !actor.isPassenger())
                && MonstrosityCasting.loadedArea(world,point(),area);
    }
    Vec3 point(){return anchor.sample().origin();}
    AABB bounds(){
        if(actor!=null)return actor.getBoundingBox();
        Vec3 p=point();return new AABB(p.x-.3,p.y,p.z-.3,p.x+.3,p.y+1.8,p.z+.3);
    }
    private boolean validEnemy(LivingEntity target) {
        return target!=null && target.isAlive() && !target.isRemoved() && target.level()==world
                && !HarbingerCasting.allied(context,target) && world.hasChunkAt(target.blockPosition())
                && target.position().distanceToSqr(point())<=32*32;
    }
    Vec3 desired(Vec3 fallback) {
        if(actor==null)return initialFacing;
        if(actor instanceof Player)return horizontal(ExecutionAnchor.viewDirection(actor),yawDirection(actor));
        if(actor instanceof Mob mob && validEnemy(mob.getTarget()))enemy=mob.getTarget();
        if(!validEnemy(enemy))enemy=HarbingerCasting.target(world,actor.getEyePosition(),ExecutionAnchor.viewDirection(actor),context);
        return horizontal(validEnemy(enemy)?enemy.position().subtract(point()):ExecutionAnchor.viewDirection(actor),yawDirection(actor));
    }
    Vec3 aimPoint(Vec3 heading) {
        return actor!=null && !(actor instanceof Player) && validEnemy(enemy)
                ?enemy.position():point().add(heading.scale(6));
    }
    LivingEntity projectileTarget(Vec3 heading) {
        if(actor instanceof Mob && validEnemy(enemy))return enemy;
        return HarbingerCasting.target(world,point().add(0,actor==null?1.62:actor.getEyeHeight(),0),heading,context);
    }
    private static Vec3 yawDirection(LivingEntity entity) {
        double yaw=Math.toRadians(entity.getViewYRot(1.0F));return new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
    }
    static Vec3 horizontal(Vec3 direction,Vec3 fallback) {
        var flat=direction.multiply(1,0,1);if(flat.lengthSqr()>1e-8)return flat.normalize();
        flat=fallback.multiply(1,0,1);return flat.lengthSqr()>1e-8?flat.normalize():new Vec3(0,0,1);
    }
    static Vec3 turn(Vec3 from,Vec3 to) {
        float current=(float)Math.toDegrees(Math.atan2(-from.x,from.z));
        float desired=(float)Math.toDegrees(Math.atan2(-to.x,to.z));
        double yaw=Math.toRadians(Mth.approachDegrees(current,desired,20));
        return new Vec3(-Math.sin(yaw),0,Math.cos(yaw));
    }
}
