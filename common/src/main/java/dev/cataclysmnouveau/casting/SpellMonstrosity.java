package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.targeting.Allies;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;

/** A detached invocation frame. Never spawned, ticked as a mob, saved or sent to clients. */
public final class SpellMonstrosity extends Netherite_Monstrosity_Entity {
    public final CastContext cast;
    public final float amplification;
    public int absorbed;
    public SpellMonstrosity(ServerLevel world,CastContext cast,Vec3 point,Vec3 direction,double amplification) {
        super(ModEntities.NETHERITE_MONSTROSITY.get(),world);
        this.cast=cast;this.amplification=Resources.amplification(amplification);NativeDamageContexts.bind(this,cast,amplification);
        setNoAi(true);setNoGravity(true);setPos(point);
        setBoundingBox(new AABB(point,point).inflate(.3));
        setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));yBodyRot=getYRot();
        // Boss phase choice uses the caster's condition, not an artificial proxy's health.
        setIsBerserk(cast.caster().getHealth()<=cast.caster().getMaxHealth()*.5F);
    }
    @Override public boolean isAlliedTo(Entity other) {
        return other==this || cast!=null && (Allies.areAllies(cast.caster(),other)
                || Allies.areAllies(cast.damageOwner(),other) || Allies.areAllies(cast.executor(),other));
    }
    @Override public boolean shouldBeSaved() {return false;}
}
