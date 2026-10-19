package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Native invocation only: never added to the world or ticked as an ordinary Boss. */
public final class SpellRemnant extends Ancient_Remnant_Entity {
    public final CastContext cast;
    public final Ability ability;
    public final List<Entity> children=new ArrayList<>();
    public SpellRemnant(ServerLevel world,CastContext cast,Ability ability,Vec3 point,Vec3 direction,double amp) {
        super(ModEntities.ANCIENT_REMNANT.get(),world);
        this.cast=cast;this.ability=ability;
        setNoAi(true);setNoGravity(true);pose(point,direction);NativeDamageContexts.bind(this,cast,amp);
    }
    public void pose(Vec3 point,Vec3 direction) {
        setPos(point);setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
        yRotO=yBodyRot=yHeadRot=yHeadRotO=getYRot();
    }
    public Goal nativeGoal(int state){return goalSelector.getAvailableGoals().stream().map(g->g.getGoal())
        .filter(g->g instanceof NativeInternalGoal n && n.confluxMaledictus$state()==state).findFirst().orElseThrow();}
    public Goal monolithGoal(){return goalSelector.getAvailableGoals().stream().map(g->g.getGoal())
        .filter(g->g.getClass().getSimpleName().equals("RemnantMonolithAttackGoal")).findFirst().orElseThrow();}
    @Override public boolean isAlliedTo(Entity other){return other==this || cast!=null && HarbingerCasting.allied(cast,other);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public net.minecraft.sounds.SoundEvent getBossMusic(){return null;}
    public void clear(){children.forEach(Entity::discard);children.clear();discard();}
}
