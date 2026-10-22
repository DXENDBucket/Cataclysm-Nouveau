package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.Ignited_Berserker_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;

/** Unspawned native invocation; real executor receives native on-hit recovery. */
public final class SpellBerserker extends Ignited_Berserker_Entity {
    public final CastContext cast;
    public SpellBerserker(ServerLevel world,CastContext cast,double amp){
        super(ModEntities.IGNITED_BERSERKER.get(),world);this.cast=cast;
        setNoAi(true);setNoGravity(true);NativeDamageContexts.bind(this,cast,amp);
    }
    public Goal nativeGoal(int state){return goalSelector.getAvailableGoals().stream().map(g->g.getGoal())
        .filter(g->g instanceof NativeInternalGoal n && n.confluxMaledictus$state()==state).findFirst().orElseThrow();}
    @Override public boolean isAlliedTo(Entity target){return target==this || cast!=null && HarbingerCasting.allied(cast,target);}
    @Override public void heal(float amount){
        if(cast==null){super.heal(amount);return;}
        var recipient=cast.executor()==null?cast.caster():cast.executor();
        if(recipient!=null && recipient.isAlive() && recipient.level()==level())recipient.heal(amount);
    }
    @Override public void lookAt(Entity target,float yaw,float pitch){if(cast==null)super.lookAt(target,yaw,pitch);}
    @Override public void playSound(SoundEvent sound,float volume,float pitch){level().playSound(null,getX(),getY(),getZ(),sound,SoundSource.PLAYERS,volume,pitch);}
    @Override public boolean shouldBeSaved(){return false;}
}
