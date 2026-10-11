package dev.cataclysmnouveau;
public enum Ability {
    AMETHYST_CLUSTER("amethyst_cluster", "Amethyst Cluster", 60),
    LIONFISH_SPIKE("lionfish_spike", "Lionfish Spike", 25),
    BOLT_STRIKE("bolt_strike", "Bolt Strike", 80),
    VOID_RUNE("void_rune", "Void Rune", 60),
    FLAME_JET("flame_jet", "Flame Jet", 50),
    CERAUNUS("ceraunus", "Ceraunus", 60),
    WAVE("wave", "Wave", 90),
    FLARE_BOMB("flare_bomb", "Flare Bomb", 300),
    LAVA_BOMB("lava_bomb", "Lava Bomb", 180),
    EARTHQUAKE("earthquake", "Monstrous Quake", 220),
    FISSURE("fissure", "Earthbreaker", 240),
    FLAME_RING("flame_ring", "Flame Rings", 300),
    SHOULDER_CHECK("shoulder_check", "Shoulder Check", 160),
    LAVA_ABSORPTION("lava_absorption", "Lava Absorption", 300),
    MONSTROUS("monstrous", "Monstrous", 120),
    BONE_FRACTURE("bone_fracture", "Bone Fracture", 40),
    LAVA_WALKING("lava_walking", "Lava Walking", 80),
    WATER_SPEAR("water_spear", "Water Spear", 90),
    LIGHTNING_SPEAR("lightning_spear", "Lightning Spear", 110),
    STORM_SERPENT("storm_serpent", "Storm Serpents", 160),
    LIGHTNING_STORM("lightning_storm", "Lightning Storm", 180),
    LIGHTNING_FIELD("lightning_field", "Lightning Field", 160),
    STORM_BURST("storm_burst", "Stormburst", 500);
    public final String id, name;
    public final int mana;
    Ability(String id, String name, int mana) { this.id=id; this.name=name; this.mana=mana; }
}
