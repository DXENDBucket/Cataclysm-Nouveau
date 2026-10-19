package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ender_Guardian_Entity;
import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Harbinger_Entity;
import com.github.L_Ender.lionfishapi.server.animation.Animation;
import dev.cataclysmnouveau.Ability;

/** Stable visual IDs for the supported native body actions; no client classes. */
public final class BodyPhantomAnimations {
    public enum Kind { SCYLLA, HARBINGER, GUARDIAN, MONSTROSITY, MALEDICTUS, REMNANT }
    /** Native Maledictus afterimage windows (the renderer proxy has no world entity ID). */
    public static boolean maledictusAfterimage(int state,int age) {
        return age>=0 && switch(state) {
            case 3,8,24,28,29 -> true;
            case 11,12,13,14 -> age>=24 && age<=33;
            case 15,16 -> age>=16 && age<=25;
            case 17 -> age>=16 && age<=24;
            default -> false;
        };
    }
    public static Kind kind(Ability ability) {
        return switch(ability) {
            case DESERT_STELE_ARRAY,REMNANT_TRIPLE_QUAKE -> Kind.REMNANT;
            case THUNDER_COMBO, ANCHOR_SPIN, ANCHOR_LEAP -> Kind.SCYLLA;
            case HARBINGER_CHARGE -> Kind.HARBINGER;
            case GRAVITY_PUNCH, GUARDIAN_UPPERCUT, GUARDIAN_BLINK, GUARDIAN_AIR_STRIKE, GUARDIAN_ROCKET_PUNCH -> Kind.GUARDIAN;
            case SHOULDER_CHECK -> Kind.MONSTROSITY;
            case HALBERD_CLEAVE, SOUL_BURST, MALEDICTUS_AERIAL_SMASH, PHANTOM_AERIAL_VOLLEY, MALEDICTUS_RUSH, MALEDICTUS_SHOCKWAVE, MALEDICTUS_UPPERCUT, MALEDICTUS_MELEE_COMBO, MALEDICTUS_SPIN_SLASHES -> Kind.MALEDICTUS;
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
        if(ability==null || age<0 || age>1200 || flags<0 || flags>(kind(ability)==Kind.MALEDICTUS?5:3)) return false;
        return switch(ability) {
            case DESERT_STELE_ARRAY -> state==17;
            case REMNANT_TRIPLE_QUAKE -> state==15;
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
            case HALBERD_CLEAVE -> state==26;
            case SOUL_BURST -> state==7;
            case MALEDICTUS_AERIAL_SMASH -> state==24 || state==25;
            case PHANTOM_AERIAL_VOLLEY -> state==3 || state==4 || state==5;
            case MALEDICTUS_RUSH -> state==10 || state==11 || state==12 || state==15 || state==16 || state==17;
            case MALEDICTUS_SHOCKWAVE -> state==9;
            case MALEDICTUS_UPPERCUT -> state==22 || state==23;
            case MALEDICTUS_MELEE_COMBO -> state==19 || state==20 || state==21;
            case MALEDICTUS_SPIN_SLASHES -> state==18;
            default -> false;
        };
    }
    private BodyPhantomAnimations() {}
}
