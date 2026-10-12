package dev.cataclysmnouveau.casting;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.Ability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;
/** Unspawned native invocation frame; never runs a Boss AI or owns the damage. */
public final class SpellIgnis extends Ignis_Entity {
    public final CastContext cast;
    public final Ability ability;
    public final double amplification;
    public int spawnedFlames;
    final java.util.Set<Entity> children=new java.util.HashSet<>();
    boolean childrenCancelled;
    java.util.function.Supplier<net.minecraft.world.entity.LivingEntity> projectileAim;
    void cancelChildren(){childrenCancelled=true;java.util.List.copyOf(children).forEach(Entity::discard);children.clear();}
    public SpellIgnis(ServerLevel world,CastContext cast,Ability ability,Vec3 point,Vec3 direction,double amp) {
        super(ModEntities.IGNIS.get(),world);
        this.cast=cast;this.ability=ability;amplification=amp;
        NativeDamageContexts.bind(this,cast,amp);
        setNoAi(true);setNoGravity(true);pose(point,direction);
        setBossPhase(IgnisCasting.phase(cast.caster()));setIsShieldBreak(getBossPhase()>1);
    }
    public void pose(Vec3 point,Vec3 direction) {
        setPos(point);setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
        yRotO=getYRot();yBodyRot=getYRot();yHeadRot=getYRot();
    }
    public Goal nativeGoal(String name) {
        return goalSelector.getAvailableGoals().stream().map(w->w.getGoal()).filter(g->g.getClass().getSimpleName().equals(name)).findFirst()
            .orElseThrow(()->new IllegalStateException("Missing native Ignis goal: "+name));
    }
    public Goal activeNativeGoal() {
        return goalSelector.getAvailableGoals().stream().map(w->w.getGoal())
            .filter(g->g instanceof com.github.L_Ender.cataclysm.entity.AnimationMonster.AI.AnimationGoal<?> && g.canUse())
            .findFirst().orElseThrow(()->new IllegalStateException("Missing native goal for Ignis animation"));
    }
    @Override public boolean isAlliedTo(Entity other){return other==this || cast!=null && HarbingerCasting.allied(cast,other);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public void heal(float amount){if(cast!=null)cast.damageOwner().heal(amount);else super.heal(amount);}
    @Override public void playSound(SoundEvent sound,float volume,float pitch){level().playSound(null,getX(),getY(),getZ(),sound,SoundSource.PLAYERS,volume,pitch);}
}
