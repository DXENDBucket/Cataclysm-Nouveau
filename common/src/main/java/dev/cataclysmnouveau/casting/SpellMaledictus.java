package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Unregistered invocation frame, not a world Boss or an independent rage meter. */
public final class SpellMaledictus extends Maledictus_Entity {
    public final CastContext cast;
    public final Ability ability;
    public final double amplification;
    final Set<Entity> children=new HashSet<>();
    final Map<Integer,List<Phantom_Halberd_Entity>> stages=new LinkedHashMap<>();
    boolean collecting,childrenCancelled;
    int collectingStage;
    public int spawnedHalberds;
    public SpellMaledictus(ServerLevel world,CastContext cast,Ability ability,Vec3 point,Vec3 heading,double amp) {
        super(ModEntities.MALEDICTUS.get(),world);
        this.cast=cast;this.ability=ability;amplification=amp;
        NativeDamageContexts.bind(this,cast,amp);setNoAi(true);setNoGravity(true);pose(point,heading);
    }
    public void pose(Vec3 point,Vec3 heading) {
        setPos(point);setYRot((float)Math.toDegrees(Math.atan2(-heading.x,heading.z)));
        yRotO=yBodyRot=yHeadRot=getYRot();
    }
    public Goal nativeGoal(int state) {
        return goalSelector.getAvailableGoals().stream().map(g->g.getGoal())
                .filter(g->g instanceof NativeInternalGoal access && access.confluxMaledictus$state()==state)
                .findFirst().orElseThrow(()->new IllegalStateException("Missing native Maledictus goal "+state));
    }
    /** ActionGeometry owns facing; native goal lookAt must not calculate impulses with a different heading. */
    @Override public void lookAt(Entity target,float yawLimit,float pitchLimit){if(cast==null)super.lookAt(target,yawLimit,pitchLimit);}
    @Override public int getRageMeter(){return cast==null?super.getRageMeter():MaledictusRage.stacks(cast.caster());}
    @Override public void setRageMeter(int value) {
        if(cast==null){super.setRageMeter(value);return;}
        if(value<=0)MaledictusRage.clear(cast.caster());
        else if(value>getRageMeter())MaledictusRage.gain(cast.caster(),value-getRageMeter());
    }
    @Override public boolean isHalfHealth(){return cast==null?super.isHalfHealth():cast.caster().getHealth()<=cast.caster().getMaxHealth()*.5F;}
    @Override public boolean isQuarterHealth(){return cast==null?super.isQuarterHealth():cast.caster().getHealth()<=cast.caster().getMaxHealth()*.25F;}
    @Override public boolean isAlliedTo(Entity other){return other==this || cast!=null && HarbingerCasting.allied(cast,other);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public void playSound(SoundEvent sound,float volume,float pitch){level().playSound(null,getX(),getY(),getZ(),sound,SoundSource.PLAYERS,volume,pitch);}
    void cancelChildren(){childrenCancelled=true;List.copyOf(children).forEach(Entity::discard);children.clear();}
}
