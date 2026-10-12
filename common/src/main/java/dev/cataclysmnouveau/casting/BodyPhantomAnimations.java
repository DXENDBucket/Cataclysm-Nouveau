package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import com.github.L_Ender.lionfishapi.server.animation.Animation;
import dev.cataclysmnouveau.Ability;

/** Stable visual IDs for the supported native body actions; no client classes. */
public final class BodyPhantomAnimations {
    public enum Kind { SCYLLA, HARBINGER, GUARDIAN, MONSTROSITY }
    public static Kind kind(Ability ability) {
        return switch(ability) {
            case THUNDER_COMBO, ANCHOR_SPIN, ANCHOR_LEAP -> Kind.SCYLLA;
            case HARBINGER_CHARGE -> Kind.HARBINGER;
            case GRAVITY_PUNCH, GUARDIAN_UPPERCUT, GUARDIAN_BLINK, GUARDIAN_AIR_STRIKE, GUARDIAN_ROCKET_PUNCH -> Kind.GUARDIAN;
            case SHOULDER_CHECK -> Kind.MONSTROSITY;
            default -> null;
        };
    }
    public static Ability ability(String id) {
        for(var a:Ability.values()) if(a.id.equals(id) && kind(a)!=null) return a;
        return null;
    }
    public static Animation animation(Kind kind,int id) {
        if(kind==Kind.HARBINGER) return id==1?The_Harbinger_Entity.CHARGE_ANIMATION:null;
        if(kind!=Kind.GUARDIAN) return null;
        return switch(id) {
            case 1 -> Ender_Guardian_Entity.GUARDIAN_RIGHT_STRONG_ATTACK;
            case 2 -> Ender_Guardian_Entity.GUARDIAN_UPPERCUT_AND_BULLET;
            case 3 -> Ender_Guardian_Entity.GUARDIAN_RAGE_UPPERCUT;
            case 4 -> Ender_Guardian_Entity.GUARDIAN_HUG_ME;
            case 5 -> Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE1;
            case 6 -> Ender_Guardian_Entity.GUARDIAN_AIR_STRIKE2;
            case 7 -> Ender_Guardian_Entity.GUARDIAN_ROCKETPUNCH;
            default -> null;
        };
    }
    public static int id(Kind kind,Animation animation) {
        for(int id=1;id<=7;id++) if(animation(kind,id)==animation && animation!=null) return id;
        return 0;
    }
    public static boolean valid(Ability ability,int state,int age,int flags) {
        if(ability==null || age<0 || age>1200 || flags<0 || flags>3) return false;
        return switch(ability) {
            case THUNDER_COMBO -> state==25;
            case ANCHOR_SPIN -> state==7;
            case ANCHOR_LEAP -> state==15 || state==17;
            case HARBINGER_CHARGE -> state==1;
            case GRAVITY_PUNCH -> state==1;
            case GUARDIAN_UPPERCUT -> state==2 || state==3;
            case GUARDIAN_BLINK -> state==4;
            case GUARDIAN_AIR_STRIKE -> state==5 || state==6;
            case GUARDIAN_ROCKET_PUNCH -> state==7;
            case SHOULDER_CHECK -> state==8;
            default -> false;
        };
    }
    private BodyPhantomAnimations() {}
}
