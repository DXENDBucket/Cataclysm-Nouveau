package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.Deepling.Deepling_Priest_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Invocation frame only: native gaze, blindness and timing run without ordinary mob AI. */
public final class SpellPriest extends Deepling_Priest_Entity {
    public final CastContext cast;
    public SpellPriest(ServerLevel world,CastContext cast,double amp){
        super(ModEntities.DEEPLING_PRIEST.get(),world);this.cast=cast;
        setNoAi(true);setNoGravity(true);NativeDamageContexts.bind(this,cast,amp);
    }
    public Goal glareGoal(){return goalSelector.getAvailableGoals().stream().map(g->g.getGoal())
        .filter(g->g.getClass().getSimpleName().equals("DeeplingLightGoal")).findFirst().orElseThrow();}
    @Override public boolean isAlliedTo(Entity target){return target==this || cast!=null && HarbingerCasting.allied(cast,target);}
    @Override public void playSound(SoundEvent sound,float volume,float pitch){level().playSound(null,getX(),getY(),getZ(),sound,SoundSource.PLAYERS,volume,pitch);}
    @Override public boolean shouldBeSaved(){return false;}
}
