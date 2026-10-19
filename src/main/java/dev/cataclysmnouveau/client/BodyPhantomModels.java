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
            case REVENANT -> new Ignited_Revenant_Entity(ModEntities.IGNITED_REVENANT.get(),level);
            case REMNANT -> new com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity(ModEntities.ANCIENT_REMNANT.get(),level);
            case MALEDICTUS -> new com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity(ModEntities.MALEDICTUS.get(),level);
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
            case Ignited_Revenant_Entity boss -> {
                boss.setIsAnger(true);boss.angerProgress=boss.prevangerProgress=5;
                boss.setAnimation(Ignited_Revenant_Entity.BONE_STORM_ATTACK);boss.setAnimationTick(Math.min(age,48));boss.tickCount=age;
            }
            case com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Ancient_Remnant.Ancient_Remnant_Entity boss -> {
                boss.setIsPower((data.flags()&1)!=0);boss.setAttackState(data.animation());boss.stopAllAnimationStates();
                boss.attackTicks=age;boss.tickCount=age;boss.getAnimationState(switch(data.animation()){
                    case 8->"right_stomp";case 10->"charge_prepare";case 11->"charge";
                    case 13->"right_double_stomp";case 15->"ground_tail";case 16->"tail_swing";default->"monolith";
                }).start(0);
            }
            case com.github.L_Ender.cataclysm.entity.InternalAnimationMonster.IABossMonsters.Maledictus.Maledictus_Entity boss -> {
                boss.setWeapon(switch(data.animation()){case 3,4->1;case 11,12,15,16,17,24,25,26->2;default->0;});boss.setRageMeter(data.flags());
                boss.setAttackState(data.animation());boss.stopAllAnimationStates();
                boss.attackTicks=age;boss.tickCount=age;boss.getAnimationState(switch(data.animation()) {
                    case 3->"flying_shoot";case 4->"fall_loop";case 5->"fall_end";
                    case 9->"flying_smash_2";case 10->"back_step";case 11->"back_step_dash";
                    case 12->"back_step_dash_no_back_step";case 15->"dash2";case 16->"dash2_no_back_step";case 17->"dash3";
                    case 18->"spin_slashes";case 19->"combo_first";case 20->"combo_first_end";case 21->"combo_second";
                    case 22->"uppercut_right";case 23->"uppercut_left";
                    case 24->"flying_halberd_smash_1";case 25->"flying_halberd_smash_2";case 26->"radagon";default->"mass_effect";
                }).start(0);
            }
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
