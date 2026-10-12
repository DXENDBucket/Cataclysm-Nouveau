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
    DEATH_LASER("death_laser", "Death Laser", 60),
    HARBINGER_CHARGE("harbinger_charge", "Harbinger Charge", 180),
    HOWITZER_VOLLEY("howitzer_volley", "Howitzer Volley", 400),
    MISSILE_SALVO("missile_salvo", "Missile Salvo", 240),
    WITHER_SMOKE("wither_smoke", "Wither Smoke", 40),
    GUARDIAN_BULLET("guardian_bullet", "Guardian Bullet", 45),
    VOID_VORTEX("void_vortex", "Void Vortex", 220),
    GUARDIAN_STOMP("guardian_stomp", "Guardian Stomp", 320),
    GRAVITY_PUNCH("gravity_punch", "Gravity Punch", 160),
    GUARDIAN_UPPERCUT("guardian_uppercut", "Guardian Uppercut Combo", 420),
    GUARDIAN_BLINK("guardian_blink", "Guardian Blink Strike", 180),
    GUARDIAN_AIR_STRIKE("guardian_air_strike", "Guardian Aerial Slam", 450),
    GUARDIAN_ROCKET_PUNCH("guardian_rocket_punch", "Guardian Rocket Punch", 160),
    IGNIS_FIREBALL("ignis_fireball", "Ignis Fireball", 120),
    IGNIS_ABYSS_FIREBALL("ignis_abyss_fireball", "Ignis Abyss Fireball", 200),
    IGNIS_BLADE_FIREBALLS("ignis_blade_fireballs", "Ignis Blade Fireballs", 320),
    IGNIS_AIR_SMASH("ignis_air_smash", "Ignis Leaping Smash", 300),
    IGNIS_SWORD_COMBO("ignis_sword_combo", "Ignis Sword Dance", 650),
    IGNIS_FIREBALL_ARRAY("ignis_fireball_array", "Ignis Five-Fireball Array", 650),
    IGNIS_EARTH_SHUDDERS("ignis_earth_shudders", "Ignis Earth Shudders", 600),
    IGNIS_REINFORCED_SMASH("ignis_reinforced_smash", "Ignis Reinforced Leaping Smash", 850),
    IGNIS_ULTIMATE("ignis_ultimate", "Ignis Ultimate Flame Slash", 900),
    IGNIS_FLAME_STRIKE("ignis_flame_strike", "Ignis Flame Strike", 280);
    public final String id, name;
    public final int mana;
    Ability(String id, String name, int mana) { this.id=id; this.name=name; this.mana=mana; }

    /** Acquisition/source families, shared by all presentation surfaces. */
    public Series series() {
        return switch(this) {
            case IGNIS_FIREBALL,IGNIS_ABYSS_FIREBALL,IGNIS_BLADE_FIREBALLS,IGNIS_AIR_SMASH,IGNIS_SWORD_COMBO,
                    IGNIS_FIREBALL_ARRAY,IGNIS_EARTH_SHUDDERS,IGNIS_REINFORCED_SMASH,IGNIS_ULTIMATE,IGNIS_FLAME_STRIKE -> Series.IGNIS;
            case GUARDIAN_BULLET,VOID_VORTEX,GUARDIAN_STOMP,GRAVITY_PUNCH,GUARDIAN_UPPERCUT,GUARDIAN_BLINK,GUARDIAN_AIR_STRIKE,GUARDIAN_ROCKET_PUNCH -> Series.ENDER_GUARDIAN;
            case LASER_BOLT,WITHER_MISSILE,HOMING_MISSILE,WITHER_HOWITZER,DEATH_LASER,HARBINGER_CHARGE,HOWITZER_VOLLEY,MISSILE_SALVO,WITHER_SMOKE -> Series.HARBINGER;
            case AMETHYST_CLUSTER -> Series.AMETHYST_CRAB;
            case LIONFISH_SPIKE -> Series.LIONFISH;
            case VOID_RUNE -> Series.ENDER_GOLEM;
            case FLAME_JET,FLARE_BOMB,LAVA_BOMB,EARTHQUAKE,FISSURE,FLAME_RING,
                    SHOULDER_CHECK,LAVA_ABSORPTION,MONSTROUS,BONE_FRACTURE,LAVA_WALKING -> Series.MONSTROSITY;
            case BOLT_STRIKE,CERAUNUS,WAVE,WATER_SPEAR,LIGHTNING_SPEAR,
                    STORM_SERPENT,LIGHTNING_STORM,LIGHTNING_FIELD,STORM_BURST,SPARK,THUNDER_CLOUD,LIGHTNING_ROW,THUNDER_COMBO,ANCHOR_SPIN,ANCHOR_LEAP,WATER_WALKING -> Series.SCYLLA;
        };
    }
    public enum Series { AMETHYST_CRAB, LIONFISH, ENDER_GOLEM, MONSTROSITY, SCYLLA, HARBINGER, ENDER_GUARDIAN, IGNIS }
}
