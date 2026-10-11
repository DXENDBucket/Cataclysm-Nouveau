package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.targeting.Allies;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Detached invocation frame, never spawned or saved as a Boss. */
public final class SpellScylla extends Scylla_Entity {
    public final CastContext cast;
    public final float bonus;
    public SpellScylla(ServerLevel world,CastContext cast,Vec3 point,float bonus) {
        super(ModEntities.SCYLLA.get(),world);this.cast=cast;this.bonus=bonus;
        setPos(point);setNoAi(true);setNoGravity(true);setPhase(0);
    }
    @Override public boolean isAlliedTo(Entity other) {
        return other==this || cast!=null && (Allies.areAllies(cast.caster(),other)
                || Allies.areAllies(cast.damageOwner(),other) || Allies.areAllies(cast.executor(),other));
    }
    @Override public boolean shouldBeSaved() {return false;}
    /** Reuse the exact goal constructed by the installed release, without copying its attack body. */
    public Goal nativeGoal(String name) {
        return goalSelector.getAvailableGoals().stream().map(wrapper->wrapper.getGoal())
                .filter(goal->goal.getClass().getSimpleName().equals(name)).findFirst()
                .orElseThrow(()->new IllegalStateException("Missing native Scylla goal: "+name));
    }
}
