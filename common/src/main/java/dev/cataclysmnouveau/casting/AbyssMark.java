package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Portal_Entity;

/** Server child ownership; only the managed flag needs to reach the client. */
public interface AbyssMark {
    boolean confluxMark$managed();
    void confluxMark$managed(boolean value);
    int confluxMark$outputTicks();
    void confluxMark$outputTicks(int ticks);
    Abyss_Blast_Portal_Entity confluxMark$portal();
    void confluxMark$portal(Abyss_Blast_Portal_Entity portal);
}
