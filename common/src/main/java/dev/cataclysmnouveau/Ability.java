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
    STORM_BURST("storm_burst", "Stormburst", 500),
    SPARK("spark", "Electric Spark", 120),
    THUNDER_CLOUD("thunder_cloud", "Thundercloud", 900),
    LIGHTNING_ROW("lightning_row", "Lightning Rows", 320),
    THUNDER_COMBO("thunder_combo", "Thunderstrike Combo", 700),
    ANCHOR_SPIN("anchor_spin", "Anchor Whirl", 300),
    ANCHOR_LEAP("anchor_leap", "Anchor Plunge", 600),
    WATER_WALKING("water_walking", "Water Walking", 60),
    LASER_BOLT("laser_bolt", "Laser Bolt", 40),
    WITHER_MISSILE("wither_missile", "Wither Missile", 90),
    HOMING_MISSILE("homing_missile", "Homing Missile", 120),
    WITHER_HOWITZER("wither_howitzer", "Wither Howitzer", 200),
    DEATH_LASER("death_laser", "Death Laser", 60);
    public final String id, name;
    public final int mana;
    Ability(String id, String name, int mana) { this.id=id; this.name=name; this.mana=mana; }

    /** Acquisition/source families, shared by all presentation surfaces. */
    public Series series() {
        return switch(this) {
            case LASER_BOLT,WITHER_MISSILE,HOMING_MISSILE,WITHER_HOWITZER,DEATH_LASER -> Series.HARBINGER;
            case AMETHYST_CLUSTER -> Series.AMETHYST_CRAB;
            case LIONFISH_SPIKE -> Series.LIONFISH;
            case VOID_RUNE -> Series.ENDER_GOLEM;
            case FLAME_JET,FLARE_BOMB,LAVA_BOMB,EARTHQUAKE,FISSURE,FLAME_RING,
                    SHOULDER_CHECK,LAVA_ABSORPTION,MONSTROUS,BONE_FRACTURE,LAVA_WALKING -> Series.MONSTROSITY;
            case BOLT_STRIKE,CERAUNUS,WAVE,WATER_SPEAR,LIGHTNING_SPEAR,
                    STORM_SERPENT,LIGHTNING_STORM,LIGHTNING_FIELD,STORM_BURST,SPARK,THUNDER_CLOUD,LIGHTNING_ROW,THUNDER_COMBO,ANCHOR_SPIN,ANCHOR_LEAP,WATER_WALKING -> Series.SCYLLA;
        };
    }
    public enum Series { AMETHYST_CRAB, LIONFISH, ENDER_GOLEM, MONSTROSITY, SCYLLA, HARBINGER }
}
