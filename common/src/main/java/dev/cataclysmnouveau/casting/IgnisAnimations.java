package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.Ignis_Entity;
import com.github.L_Ender.lionfishapi.server.animation.Animation;

/** Stable adapter protocol IDs, resolved to the installed native animations on each side. */
public final class IgnisAnimations {
    private static final Animation[] ACTIONS={
        Ignis_Entity.COMBO1,Ignis_Entity.COMBO2,Ignis_Entity.SMASH_IN_AIR,Ignis_Entity.SMASH,
        Ignis_Entity.REINFORCED_SMASH_IN_AIR,Ignis_Entity.REINFORCED_SMASH_IN_AIR_SOUL,
        Ignis_Entity.REINFORCED_SMASH,Ignis_Entity.REINFORCED_SMASH_SOUL,
        Ignis_Entity.EARTH_SHUDDERS_ATTACK,Ignis_Entity.ULTIMATE_ATTACK
    };
    public static int id(Animation animation){for(int i=0;i<ACTIONS.length;i++)if(ACTIONS[i]==animation)return i+1;return 0;}
    public static Animation animation(int id){return id>0 && id<=ACTIONS.length?ACTIONS[id-1]:null;}
    /** Airborne goals can hold their final pose until actual landing or action timeout. */
    public static int maxProgress(int id){
        var animation=animation(id);if(animation==null)return -1;
        return id==3?Math.max(200,animation.getDuration()):id==5 || id==6?Math.max(280,animation.getDuration()):animation.getDuration();
    }
    private IgnisAnimations(){}
}
