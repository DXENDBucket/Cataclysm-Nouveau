package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.arsconflux.api.targeting.Allies;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

/** Detached native attack invocation; never spawned, saved, or used as damage owner. */
public final class SpellHarbinger extends The_Harbinger_Entity {
    public final CastContext cast;
    public final Ability ability;
    public final float amplification;
    public final double emitterLift;
    private final boolean powered;
    public boolean acceptingRepeat, repeatRequested;
    public SpellHarbinger(ServerLevel world,CastContext cast,Ability ability,Vec3 point,double lift,double amplify) {
        super(ModEntities.THE_HARBINGER.get(),world);
        this.cast=cast;this.ability=ability;amplification=Resources.amplification(amplify);emitterLift=lift;NativeDamageContexts.bind(this,cast,amplification);
        powered=cast.caster().getHealth()<=cast.caster().getMaxHealth()/2;
        setNoAi(true);setNoGravity(true);pose(point,cast.caster().getLookAngle());
    }
    public void pose(Vec3 point,Vec3 direction) {
        setPos(point.add(0,-emitterLift,0));setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
        yBodyRot=getYRot();yHeadRot=getYRot();setXRot((float)-Math.toDegrees(Math.atan2(direction.y,direction.horizontalDistance())));
    }
    @Override public boolean isPowered(){return powered;}
    @Override public boolean isAlliedTo(Entity other){return other==this || cast!=null && HarbingerCasting.allied(cast,other);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public void playSound(SoundEvent sound,float volume,float pitch){
        var point=position().add(0,emitterLift,0);level().playSound(null,point.x,point.y,point.z,sound,SoundSource.PLAYERS,volume,pitch);
    }
    public Goal nativeGoal(String name) {
        return goalSelector.getAvailableGoals().stream().map(w->w.getGoal()).filter(g->g.getClass().getSimpleName().equals(name)).findFirst()
                .orElseThrow(()->new IllegalStateException("Missing native Harbinger goal: "+name));
    }
}
