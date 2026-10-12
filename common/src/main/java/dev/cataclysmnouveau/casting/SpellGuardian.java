package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.arsconflux.api.context.CastContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.*;

/** Detached native invocation frame, never spawned or used as the damage owner. */
public final class SpellGuardian extends Ender_Guardian_Entity {
    public final CastContext cast;
    public final double amplification;
    public SpellGuardian(ServerLevel world,CastContext cast,Vec3 point,Vec3 direction,double amp) {
        super(ModEntities.ENDER_GUARDIAN.get(),world);
        this.cast=cast;amplification=amp;NativeDamageContexts.bind(this,cast,amp);
        setNoAi(true);setNoGravity(true);pose(point,direction);setBoundingBox(new AABB(point,point).inflate(.3));
        setIsHelmetless(cast.caster().getHealth()<=cast.caster().getMaxHealth()*.5F);
    }
    public void pose(Vec3 point,Vec3 direction){setPos(point);setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));yBodyRot=getYRot();yHeadRot=getYRot();}
    @Override public boolean isAlliedTo(Entity other){return other==this || cast!=null && HarbingerCasting.allied(cast,other);}
    @Override public boolean shouldBeSaved(){return false;}
    @Override public void playSound(SoundEvent sound,float volume,float pitch){level().playSound(null,getX(),getY(),getZ(),sound,SoundSource.PLAYERS,volume,pitch);}
}
