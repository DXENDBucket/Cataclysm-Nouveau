package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignited_Revenant_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Invocation frame only; never spawned or ticked as an ordinary Revenant. */
public final class SpellRevenant extends Ignited_Revenant_Entity {
    public final CastContext cast;
    public final double amplification;
    public final List<Entity> children=new ArrayList<>();
    public SpellRevenant(ServerLevel world,CastContext cast,Vec3 point,Vec3 facing,double amp){
        super(ModEntities.IGNITED_REVENANT.get(),world);this.cast=cast;amplification=amp;
        setNoAi(true);setNoGravity(true);setIsAnger(true);pose(point,facing);
    }
    public void pose(Vec3 point,Vec3 direction){
        setPos(point);setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
        yRotO=yBodyRot=yHeadRot=yHeadRotO=getYRot();
    }
    public Goal stormGoal(){return goalSelector.getAvailableGoals().stream().map(g->g.getGoal())
        .filter(g->g.getClass().getSimpleName().equals("BoneStormGoal")).findFirst().orElseThrow();}
    @Override public boolean isAlliedTo(Entity target){return target==this || cast!=null && HarbingerCasting.allied(cast,target);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public net.minecraft.sounds.SoundEvent getBossMusic(){return null;}
    public void clear(){children.forEach(Entity::discard);children.clear();discard();}
}
