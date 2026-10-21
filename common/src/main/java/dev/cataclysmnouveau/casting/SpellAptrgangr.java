package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Draugar.Aptrgangr_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.execution.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import java.util.*;

/** Unspawned native smash frame; its child sessions can outlive a normally completed body action. */
public final class SpellAptrgangr extends Aptrgangr_Entity {
    public final CastContext cast;
    public final ExecutionAnchor anchor;
    public final double amplification;
    public final List<ExecutionSession<?>> children=new ArrayList<>();
    public SpellAptrgangr(ServerLevel world,CastContext cast,ExecutionAnchor anchor,double amp){
        super(ModEntities.APTRGANGR.get(),world);this.cast=cast;this.anchor=anchor;amplification=amp;
        setPos(anchor.sample().origin());setNoAi(true);setNoGravity(true);NativeDamageContexts.bind(this,cast,amp);
    }
    public Goal smashGoal(){return goalSelector.getAvailableGoals().stream().map(g->g.getGoal())
        .filter(g->g instanceof NativeInternalGoal n && n.confluxMaledictus$state()==2).findFirst().orElseThrow();}
    @Override public boolean isAlliedTo(Entity target){return target==this || cast!=null && HarbingerCasting.allied(cast,target);}
    @Override public boolean shouldBeSaved(){return false;}
}
