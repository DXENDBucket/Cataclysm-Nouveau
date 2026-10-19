package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.The_Leviathan_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Unspawned invocation frame: native goals own distribution, never world Boss AI. */
public final class SpellLeviathan extends The_Leviathan_Entity {
    public final CastContext cast;
    public final Ability ability;
    public final double amplification,duration;
    public final List<Entity> children=new ArrayList<>();
    public boolean collecting;
    public boolean childrenCancelled;
    public int candidates;
    public SpellLeviathan(ServerLevel world,CastContext cast,Ability ability,Vec3 point,double amp,double duration){
        super(ModEntities.THE_LEVIATHAN.get(),world);
        this.cast=cast;this.ability=ability;amplification=amp;this.duration=duration;
        setPos(point);setNoAi(true);setNoGravity(true);
    }
    public Goal nativeGoal(){
        String name=switch(ability){case ABYSS_MINEFIELD->"LeviathanMineAttackGoal";case ABYSS_TRIPLE_BLAST->"LeviathanBlastFireAttackGoal";default->"LeviathanAbyssBlastPortalAttackGoal";};
        return goalSelector.getAvailableGoals().stream().map(g->g.getGoal()).filter(g->g.getClass().getSimpleName().equals(name))
                .findFirst().orElseThrow(()->new IllegalStateException("Missing native Leviathan goal: "+name));
    }
    @Override public boolean isAlliedTo(Entity other){return other==this || cast!=null && HarbingerCasting.allied(cast,other);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public net.minecraft.sounds.SoundEvent getBossMusic(){return null;}
    public void clearChildren(){childrenCancelled=true;for(var child:children)LeviathanFormations.remove(child);children.clear();}
}
