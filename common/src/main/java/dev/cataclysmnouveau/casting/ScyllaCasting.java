package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.effect.*;
import com.github.L_Ender.cataclysm.entity.projectile.*;
import com.github.L_Ender.cataclysm.init.*;
import dev.arsconflux.api.context.CastContext;
import dev.cataclysmnouveau.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.phys.*;

/** Installed native entities own animation, attack windows and expiry. */
public final class ScyllaCasting {
    public static boolean supports(Ability ability) {
        return switch(ability) {case STORM_SERPENT,LIGHTNING_STORM,LIGHTNING_FIELD,STORM_BURST->true;default->false;};
    }
    public static <T extends Entity> T manage(T entity,CastContext cast) {
        ((ScyllaPayload)entity).confluxScylla$cast(cast);
        if(entity instanceof Lightning_Storm_Entity storm) storm.setCaster(cast.damageOwner());
        if(entity instanceof Lightning_Area_Effect_Entity field) field.setOwner(cast.damageOwner());
        if(entity instanceof Storm_Serpent_Entity snake) snake.setCaster(cast.damageOwner());
        if(entity instanceof Spark_Entity spark) spark.setOwner(cast.damageOwner());
        return entity;
    }
    public static Entity bossChild(Entity entity,SpellScylla boss) {
        if(entity instanceof Lightning_Storm_Entity storm) {
            storm.setDamage(Math.max(0,CataclysmConfig.scyllaStormDamage()+boss.bonus));
            storm.setHpDamage(CataclysmConfig.scyllaHpDamage());
        } else if(entity instanceof Spark_Entity spark) {
            spark.setDamage(Math.max(0,CataclysmConfig.scyllaStormDamage()+boss.bonus));
            spark.setAreaDamage(Math.max(0,CataclysmConfig.scyllaFieldDamage()+boss.bonus));
            spark.setHpDamage(CataclysmConfig.scyllaHpDamage());
        }
        return entity instanceof ScyllaPayload?manage(entity,boss.cast):entity;
    }
    public static boolean effect(Ability ability,ServerLevel world,CastContext cast,double amp,double duration) {
        Vec3 point=NativePayloads.groundPoint(world,cast);
        if(point==null) return false;
        float bonus=Resources.damageBonus(amp);
        if(ability==Ability.LIGHTNING_STORM) return world.addFreshEntity(manage(new Lightning_Storm_Entity(
                world,point.x,point.y,point.z,0,CataclysmConfig.scyllaStormDelay(),
                Math.max(0,CataclysmConfig.scyllaStormDamage()+bonus),CataclysmConfig.scyllaHpDamage(),cast.damageOwner(),2),cast));
        if(ability==Ability.LIGHTNING_FIELD) {
            var field=manage(new Lightning_Area_Effect_Entity(world,point.x,point.y,point.z),cast);
            field.setRadius(3);field.setDamage(Math.max(0,CataclysmConfig.scyllaFieldDamage()+bonus));
            field.setWaitTime(12);int ticks=MonstrosityCasting.scaledTicks(CataclysmConfig.scyllaFieldDuration(),duration);
            field.setDuration(ticks);field.setRadiusPerTick(-2.5F/ticks);
            return world.addFreshEntity(field);
        }
        if(ability==Ability.STORM_SERPENT) {
            // A hostile selection aims the snakes; selecting self/block uses the trigger's direction.
            LivingEntity target=cast.target() instanceof EntityHitResult hit && hit.getEntity() instanceof LivingEntity living
                    && !dev.arsconflux.api.targeting.Allies.areAllies(cast.damageOwner(),living)?living:null;
            Vec3 forward=MonstrosityCasting.direction(cast).multiply(1,0,1).normalize();
            if(forward.lengthSqr()<1e-8) forward=new Vec3(0,0,1);
            if(!MonstrosityCasting.loadedArea(world,point,12)) return false;
            boolean self=cast.target() instanceof EntityHitResult hit && hit.getEntity()==cast.caster();
            Vec3 base=self?point:point.subtract(forward.scale(8));
            Vec3 side=new Vec3(-forward.z,0,forward.x);boolean spawned=false;
            for(int i=0;i<2;i++) {
                Vec3 origin=base.add(side.scale(i==0?4:-4));
                Vec3 heading=self?forward:point.subtract(origin).multiply(1,0,1).normalize();
                float yaw=(float)Math.atan2(heading.z,heading.x);
                var snake=new Storm_Serpent_Entity(world,origin.x,origin.y,origin.z,yaw,i*8,cast.damageOwner(),
                        Math.max(0,CataclysmConfig.scyllaSnakeDamage()+bonus),target,i==0);
                spawned|=world.addFreshEntity(manage(snake,cast));
            }
            return spawned;
        }
        if(ability==Ability.STORM_BURST) {
            if(!MonstrosityCasting.loadedArea(world,point,14)) return false;
            var frame=new SpellScylla(world,cast,point,bonus);
            frame.setAttackState(17);frame.attackTicks=1;
            frame.aiStep(); // Native 7 x 5 delayed lightning pattern and two electric sparks.
            world.sendParticles(ModParticle.LIGHTNING_EXPLODE.get(),point.x,point.y+.03,point.z,1,0,0,0,0);
            world.playSound(null,point.x,point.y,point.z,ModSounds.EXPLOSION.get(),SoundSource.PLAYERS,1,1);
            return true;
        }
        return false;
    }
    private ScyllaCasting() {}
}
