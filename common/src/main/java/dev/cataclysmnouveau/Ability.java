package dev.cataclysmnouveau;
public enum Ability {
    AMETHYST_CLUSTER("amethyst_cluster", "Amethyst Cluster", 60),
    LIONFISH_SPIKE("lionfish_spike", "Lionfish Spike", 25),
    BOLT_STRIKE("bolt_strike", "Bolt Strike", 80),
    VOID_RUNE("void_rune", "Void Rune", 60),
    FLAME_JET("flame_jet", "Flame Jet", 50),
    CERAUNUS("ceraunus", "Ceraunus", 60),
    WAVE("wave", "Wave", 90),
    FLARE_BOMB("flare_bomb", "Flare Bomb", 300);
    public final String id, name;
    public final int mana;
    Ability(String id, String name, int mana) { this.id=id; this.name=name; this.mana=mana; }
}
