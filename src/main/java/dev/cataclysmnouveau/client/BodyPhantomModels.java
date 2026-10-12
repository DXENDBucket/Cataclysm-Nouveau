package dev.cataclysmnouveau.client;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.*;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Scylla.Scylla_Entity;
import com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.NewNetherite_Monstrosity.Netherite_Monstrosity_Entity;
import com.github.L_Ender.cataclysm.client.animation.Scylla_Projectile_Animation;
import com.github.L_Ender.cataclysm.init.ModEntities;
import dev.cataclysmnouveau.casting.BodyPhantomAnimations;
import dev.cataclysmnouveau.casting.BodyPhantomAnimations.Kind;
import dev.cataclysmnouveau.network.BodyPhantomPayload;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Mob;

/** Native models and layers are resolved at runtime. Never ticks or spawns a Boss. */
final class BodyPhantomModels {
    static Mob create(Kind kind,ClientLevel level) {
        Mob proxy=switch(kind) {
            case SCYLLA -> new Scylla_Entity(ModEntities.SCYLLA.get(),level);
            case HARBINGER -> new The_Harbinger_Entity(ModEntities.THE_HARBINGER.get(),level);
            case GUARDIAN -> new Ender_Guardian_Entity(ModEntities.ENDER_GUARDIAN.get(),level);
            case MONSTROSITY -> new Netherite_Monstrosity_Entity(ModEntities.NETHERITE_MONSTROSITY.get(),level);
        };
        proxy.setNoAi(true);proxy.setNoGravity(true);proxy.setSilent(true);proxy.noCulling=true;
        return proxy;
    }
    static void pose(Mob proxy,BodyPhantomPayload data,int age) {
        switch(proxy) {
            case Scylla_Entity boss -> {
                boss.setPhase(0);boss.setEye((data.flags()&1)!=0);boss.setFlying((data.flags()&2)!=0);
                boss.setAttackState(data.animation());boss.stopAllAnimationStates();
                String key=switch(data.animation()) {case 7 -> "spin";case 15 -> "chain_jump_1";case 17 -> "chain_jump_3";default -> "whip_and_spear";};
                // Hold the native airborne end pose until the real actor lands.
                if(data.animation()==15)age=Math.min(age,Math.max(0,(int)(Scylla_Projectile_Animation.CHAIN_JUMP.lengthInSeconds()*20)-1));
                boss.attackTicks=age;boss.tickCount=age;boss.getAnimationState(key).start(0);
            }
            case Netherite_Monstrosity_Entity boss -> {
                boss.setIsBerserk((data.flags()&1)!=0);boss.setAttackState(data.animation());boss.stopAllAnimationStates();
                // The real shoulder action repeats this native charge window under Extend Time.
                age=20+Math.floorMod(age-20,29);
                boss.attackTicks=age;boss.tickCount=age;boss.getAnimationState("shoulder_check").start(0);
            }
            case Ender_Guardian_Entity boss -> {
                boss.setIsHelmetless((data.flags()&1)!=0);
                var animation=BodyPhantomAnimations.animation(Kind.GUARDIAN,data.animation());
                boss.setAnimation(animation);boss.setAnimationTick(Math.min(age,animation.getDuration()-1));boss.tickCount=age;
            }
            case The_Harbinger_Entity boss -> {
                boss.setHealth(boss.getMaxHealth()*((data.flags()&1)!=0?.4F:1F));boss.setIsCharge((data.flags()&2)!=0);
                boss.deactivateProgress=boss.prevdeactivateProgress=0;
                boss.Laser_Mode_Progress=boss.prev_Laser_Mode_Progress=0;
                var animation=BodyPhantomAnimations.animation(Kind.HARBINGER,data.animation());
                boss.setAnimation(animation);boss.setAnimationTick(Math.min(age,animation.getDuration()-1));boss.tickCount=age;
            }
            default -> throw new IllegalArgumentException("Unsupported phantom proxy");
        }
    }
    private BodyPhantomModels() {}
}
