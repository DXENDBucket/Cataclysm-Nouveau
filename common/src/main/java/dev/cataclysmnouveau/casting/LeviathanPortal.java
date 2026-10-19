package dev.cataclysmnouveau.casting;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Portal_Abyss_Blast_Entity;

public interface LeviathanPortal extends LeviathanArea {
    void confluxLeviathan$beamDuration(int ticks);
    Portal_Abyss_Blast_Entity confluxLeviathan$child();
    void confluxLeviathan$child(Portal_Abyss_Blast_Entity child);
}
