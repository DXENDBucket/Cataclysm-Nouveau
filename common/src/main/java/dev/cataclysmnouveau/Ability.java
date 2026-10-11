package dev.cataclysmnouveau;
public enum Ability {
    AMETHYST_CLUSTER("amethyst_cluster", "Amethyst Cluster", 60),
    LIONFISH_SPIKE("lionfish_spike", "Lionfish Spike", 25),
    BOLT_STRIKE("bolt_strike", "Bolt Strike", 80);
    public final String id, name;
    public final int mana;
    Ability(String id, String name, int mana) { this.id=id; this.name=name; this.mana=mana; }
}
