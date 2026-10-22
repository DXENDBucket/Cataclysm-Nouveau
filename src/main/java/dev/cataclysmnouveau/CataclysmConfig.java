package dev.cataclysmnouveau;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.EnumMap;
public final class CataclysmConfig {
    public static final ModConfigSpec SPEC;
    private static final EnumMap<Ability, ModConfigSpec.IntValue> COSTS = new EnumMap<>(Ability.class);
    private static final ModConfigSpec.IntValue ANCHOR_RANGE;
    private static final ModConfigSpec.BooleanValue HARBINGER_FIRE,IGNIS_FIRE;
    private static final ModConfigSpec.IntValue HARBINGER_RANGE;
    private static final ModConfigSpec.IntValue GUARDIAN_BULLET_DELAY,GUARDIAN_VORTEX_LIFETIME,GUARDIAN_ACTION_RANGE;
    private static final ModConfigSpec.DoubleValue AUGMENT_FRACTION,AUGMENT_MIN,AUGMENT_MAX;
    private static final ModConfigSpec.BooleanValue CAP_HEALTH_DAMAGE;
    private static final ModConfigSpec.IntValue AMPLIFY_COST, BOLT_WARMUP;
    private static final ModConfigSpec.DoubleValue BOLT_DAMAGE;
    private static final EnumMap<Ability, ModConfigSpec.DoubleValue> GROUND_DAMAGE = new EnumMap<>(Ability.class);
    private static final EnumMap<Ability, ModConfigSpec.IntValue> GROUND_WARMUP = new EnumMap<>(Ability.class);
    private static final ModConfigSpec.DoubleValue ANCHOR_DAMAGE, WAVE_DAMAGE;
    private static final ModConfigSpec.IntValue WAVE_LIFETIME;
    private static final ModConfigSpec.DoubleValue FLARE_DIRECT_DAMAGE, FLARE_JET_DAMAGE;
    private static final ModConfigSpec.DoubleValue QUAKE_RADIUS, ABSORPTION_HEAL, CHARGE_SPEED;
    private static final ModConfigSpec.IntValue LAVA_LIFETIME, FRACTURE_DURATION, LAVA_WALKING_DURATION;
    private static final ModConfigSpec.BooleanValue COOLDOWNS, CREATIVE_COOLDOWN;
    private static final EnumMap<Ability, ModConfigSpec.IntValue> COOLDOWN_TICKS = new EnumMap<>(Ability.class);
    private static final ModConfigSpec.DoubleValue SCYLLA_SPEAR, SCYLLA_STORM, SCYLLA_FIELD, SCYLLA_SNAKE, SCYLLA_HP;
    private static final ModConfigSpec.IntValue SCYLLA_DELAY, SCYLLA_FIELD_DURATION;
    private static final ModConfigSpec.IntValue SCYLLA_CLOUD_WARMUP;
    private static final ModConfigSpec.IntValue WATER_WALKING_DURATION;
    private static final ModConfigSpec.DoubleValue ANCHOR_PULL_SPEED;
    private static final ModConfigSpec.IntValue REMNANT_STORM_LIFETIME;
    private static final ModConfigSpec.IntValue RAGE_DECAY,RAGE_GAIN,HALBERD_WARMUP;
    static {
        var b = new ModConfigSpec.Builder(); b.push("mana");
        for (var a : Ability.values()) COSTS.put(a, b.comment("Ars mana per actual launch or repeated impact.").defineInRange(a.id, a.mana, 0, 100000));
        AMPLIFY_COST = b.defineInRange("per_amplify", 10, 0, 100000); b.pop();
        b.push("bolt_strike");
        BOLT_DAMAGE = b.comment("Addon balance value for native bolt entity base damage.").defineInRange("damage", 16.0, 0, 100000);
        BOLT_WARMUP = b.comment("Native warmup ticks; damage follows 12 ticks later.").defineInRange("warmup_ticks", 34, 0, 1200);
        b.pop();
        for (var a : new Ability[]{Ability.VOID_RUNE, Ability.FLAME_JET}) {
            b.push(a.id);
            GROUND_DAMAGE.put(a, b.comment("Damage per native hit. -1 follows Cataclysm's own ability configuration.")
                    .defineInRange("damage", -1.0, -1, 100000));
            GROUND_WARMUP.put(a, b.comment("Initial delay; native attack animation then proceeds normally.")
                    .defineInRange("warmup_ticks", 10, 0, 1200));
            b.pop();
        }
        b.push("ceraunus");
        ANCHOR_DAMAGE=b.comment("-1 uses Ceraunus weapon attack damage plus the player's native base 1, without caster melee gear scaling.")
                .defineInRange("damage",-1.0,-1,100000);b.pop();
        b.push("wave");
        WAVE_DAMAGE=b.comment("Default 9 matches Scylla's base wave damage. -1 follows the Ceraunus weapon wave configuration.")
                .defineInRange("damage",9.0,-1,100000);
        WAVE_LIFETIME=b.comment("Native wave lifespan for one-shot carriers before Extend Time (+50% per modifier); repeating carriers follow their own lifespan.")
                .defineInRange("lifetime_ticks",80,40,1200);b.pop();
        b.push("flare_bomb");
        FLARE_DIRECT_DAMAGE=b.comment("Direct native hit damage; Harm amplification also applies to the generated jets.")
                .defineInRange("direct_damage",7.0,0,100000);
        FLARE_JET_DAMAGE=b.comment("Damage per generated jet; each impact creates the native four-arm pattern, up to 40 jets.")
                .defineInRange("jet_damage",7.0,0,100000);b.pop();
        b.push("monstrosity");
        QUAKE_RADIUS=b.comment("Native Boss quake centered at the resolved location.").defineInRange("quake_radius",6.25,1,32);
        ABSORPTION_HEAL=b.comment("Healing per absorption wave that actually removes lava. There are three native waves.").defineInRange("absorption_heal",15.0,0,100000);
        CHARGE_SPEED=b.comment("Horizontal blocks per tick during the collision-limited shoulder charge.").defineInRange("charge_speed",.75,.1,2.0);
        LAVA_LIFETIME=b.comment("Lifetime of each native grounded lava bomb; managed sources cannot spread.").defineInRange("lava_lifetime_ticks",200,1,1200);b.pop();
        b.push("bone_fracture");
        FRACTURE_DURATION=b.comment("Base duration of the native fracture effect, before duration augments.").defineInRange("duration_ticks",100,2,1200);b.pop();
        b.push("lava_walking");
        LAVA_WALKING_DURATION=b.comment("Base duration of lava surface walking and lava/burning protection; crouching allows diving.").defineInRange("duration_ticks",600,2,1200);b.pop();
        b.push("scylla");
        SCYLLA_SPEAR=b.comment("Direct damage for both native spears; -1 follows Scylla SpearDamage.").defineInRange("spear_damage",-1.0,-1,100000);
        SCYLLA_STORM=b.comment("Base damage per lightning strike; -1 follows Scylla LightningStormDamage.").defineInRange("storm_damage",-1.0,-1,100000);
        SCYLLA_FIELD=b.comment("Damage per native electric-field hit; -1 follows Scylla LightningAreaDamage.").defineInRange("field_damage",-1.0,-1,100000);
        SCYLLA_SNAKE=b.comment("Damage per serpent strike; -1 follows Scylla SnakeDamage.").defineInRange("serpent_damage",-1.0,-1,100000);
        SCYLLA_HP=b.comment("Maximum-health fraction added by lightning storms; -1 follows Scylla StormHpDamage.").defineInRange("storm_health_fraction",-1.0,-1,1);
        SCYLLA_DELAY=b.comment("Initial delay for the standalone storm, before its native strike animation.").defineInRange("storm_delay_ticks",10,0,1200);
        SCYLLA_FIELD_DURATION=b.comment("Standalone electric field duration before duration augments; radius shrinks across this interval.").defineInRange("field_duration_ticks",160,20,1200);
        SCYLLA_CLOUD_WARMUP=b.comment("Cloud formation delay before native water/lightning spear rain. Default shortens the Boss windup; use 115 for native timing.").defineInRange("cloud_warmup_ticks",40,0,1200);
        ANCHOR_RANGE=b.comment("Sight distance of the action hook, re-sampled when the anchor launches; never loads chunks.").defineInRange("anchor_range",64,4,128);
        ANCHOR_PULL_SPEED=b.comment("Blocks per tick for the collision-limited anchor plunge pull.").defineInRange("anchor_pull_speed",2.0,.1,3);
        WATER_WALKING_DURATION=b.comment("Water surface walking duration before time augments.").defineInRange("water_walking_duration_ticks",600,2,1200);b.pop();
        b.push("harbinger");
        HARBINGER_FIRE=b.comment("Allow managed laser impacts to place fire; entity ignition still follows native behavior. No glass destruction or EMP reset.").define("terrain_fire",false);
        HARBINGER_RANGE=b.comment("Loaded, visible target ray for native homing launches.").defineInRange("homing_range",48,4,128);b.pop();
        b.push("ignis");IGNIS_FIRE=b.comment("Allow native Ignis explosions to leave terrain fire; block destruction remains disabled.").define("terrain_fire",false);b.pop();
        b.push("ender_guardian");
        GUARDIAN_ACTION_RANGE=b.comment("Maximum target distance for Guardian actor actions; never loads extra chunks.").defineInRange("action_target_range",32,4,128);
        GUARDIAN_BULLET_DELAY=b.comment("Preparation ticks before the bullet leaves its staging position. Special Ars trajectories keep their own motion.").defineInRange("bullet_preparation_ticks",30,1,200);
        GUARDIAN_VORTEX_LIFETIME=b.comment("Native vortex lifespan before duration augments; collapse explodes without terrain damage.").defineInRange("vortex_lifetime_ticks",300,40,1200);b.pop();
        b.push("maledictus");
        RAGE_DECAY=b.comment("Ticks before losing one rage level; gaining rage refreshes this interval. Maximum five levels.").defineInRange("rage_decay_ticks",200,1,72000);
        RAGE_GAIN=b.comment("Rage levels gained by the active glyph before potency augments.").defineInRange("rage_stacks_per_cast",1,1,5);
        HALBERD_WARMUP=b.comment("Initial ground-halberd delay before its native animation and damage window.").defineInRange("halberd_warmup_ticks",10,0,1200);b.pop();
        b.push("ancient_remnant");
        REMNANT_STORM_LIFETIME=b.comment("Base lifetime of the three native Boss storms, before duration augments.").defineInRange("storm_lifetime_ticks",300,20,1200);b.pop();
        b.push("cooldowns");
        COOLDOWNS=b.comment("Gate new active uses through Conflux; existing trajectories keep running.").define("enabled",true);
        CREATIVE_COOLDOWN=b.define("creative_cooldown",false);
        b.push("ticks");
        for(var a:Ability.values()) COOLDOWN_TICKS.put(a,b.defineInRange(a.id,defaultCooldown(a),0,72000));
        b.pop();b.pop();
        b.push("damage");
        CAP_HEALTH_DAMAGE=b.comment("Cap otherwise uncapped target maximum-health damage at the fixed damage of the same hit, before augments and Ars scaling. Only addon casts; false preserves native formulas and existing native caps.").define("cap_health_damage",true);b.pop();
        b.push("damage_augments");
        AUGMENT_FRACTION=b.comment("Base native hit fraction per Amplify/Dampen, before Ars equipment scaling. Each hit segment uses its own unaugmented base.").defineInRange("fraction",.20,0,1);
        AUGMENT_MIN=b.defineInRange("minimum",2.0,0,100000);
        AUGMENT_MAX=b.defineInRange("maximum",10.0,0,100000);b.pop();
        SPEC=b.build();
    }
    public static int remnantStormLifetime(){return SPEC.isLoaded()?REMNANT_STORM_LIFETIME.get():300;}
    public static int rageDecay(){return SPEC.isLoaded()?RAGE_DECAY.get():200;}
    public static int rageGain(){return SPEC.isLoaded()?RAGE_GAIN.get():1;}
    public static int halberdWarmup(){return SPEC.isLoaded()?HALBERD_WARMUP.get():10;}
    public static int cost(Ability a) { return SPEC.isLoaded()?COSTS.get(a).get():a.mana; }
    public static boolean capHealthDamage() { return !SPEC.isLoaded() || CAP_HEALTH_DAMAGE.get(); }
    public static int amplifyCost() { return SPEC.isLoaded()?AMPLIFY_COST.get():10; }
    public static float boltDamage() { return SPEC.isLoaded()?BOLT_DAMAGE.get().floatValue():16; }
    public static int boltWarmup() { return SPEC.isLoaded()?BOLT_WARMUP.get():34; }
    public static float groundDamage(Ability ability) {
        double configured = SPEC.isLoaded() ? GROUND_DAMAGE.get(ability).get() : -1;
        if (configured >= 0) return (float)configured;
        return switch (ability) {
            case VOID_RUNE -> (float)com.github.L_Ender.cataclysm.config.CMCommonConfig.VoidCore.runeDamage;
            case FLAME_JET -> (float)com.github.L_Ender.cataclysm.config.CMCommonConfig.NetheriteMonstrosity.FlameJetDamage;
            default -> throw new IllegalArgumentException("Not a ground effect: " + ability);
        };
    }
    public static int groundWarmup(Ability ability) {
        return SPEC.isLoaded() ? GROUND_WARMUP.get(ability).get() : 10;
    }
    public static float anchorDamage() {
        double value=SPEC.isLoaded()?ANCHOR_DAMAGE.get():-1;
        return (float)(value>=0?value:1+com.github.L_Ender.cataclysm.config.CMCommonConfig.Ceraunus.attackDamage);
    }
    public static float waveDamage() {
        double value=SPEC.isLoaded()?WAVE_DAMAGE.get():9;
        return (float)(value>=0?value:com.github.L_Ender.cataclysm.config.CMCommonConfig.Ceraunus.waveDamage);
    }
    public static int waveLifetime() { return SPEC.isLoaded()?WAVE_LIFETIME.get():80; }
    public static float flareDirectDamage() {return SPEC.isLoaded()?FLARE_DIRECT_DAMAGE.get().floatValue():7;}
    public static float flareJetDamage() {return SPEC.isLoaded()?FLARE_JET_DAMAGE.get().floatValue():7;}
    private static int defaultCooldown(Ability ability) {
        return switch(ability) {case BERSERKER_CROSS_SLASH->60;case BERSERKER_SWORD_DANCE->80;case BERSERKER_SPIN->160;case APTRGANGR_SMASH->80;case ASHEN_BREATH->80;case BONE_STORM->160;case REMNANT_CHARGE->240;case REMNANT_TAIL_SWEEP->160;case DESERT_STELE_ARRAY->320;case REMNANT_TRIPLE_QUAKE->200;case SANDSTORM_RING->240;case REMNANT_STOMP->160;case ABYSS_ORB_ARRAY->160;case ABYSS_TRIPLE_BLAST->240;case DIMENSIONAL_RIFT->400;case TIDAL_TENTACLE->100;case ABYSS_MINEFIELD->240;case ABYSS_BOMBARDMENT_ARRAY->400;case ABYSS_ORB->0;case ABYSS_MINE->20;case ABYSS_BLAST->200;case ABYSS_BOMBARDMENT->160;case MALEDICTUS_AERIAL_SMASH->100;case PHANTOM_AERIAL_VOLLEY->100;case MALEDICTUS_RUSH->80;case MALEDICTUS_SHOCKWAVE->100;case MALEDICTUS_UPPERCUT->80;case MALEDICTUS_MELEE_COMBO->80;case MALEDICTUS_SPIN_SLASHES->100;case PHANTOM_SPIRAL->120;case PHANTOM_RINGS->100;case HALBERD_CLEAVE->250;case SOUL_BURST->150;case PHANTOM_HALBERD->0;case IGNIS_FIREBALL_ARRAY->160;case IGNIS_EARTH_SHUDDERS->240;case IGNIS_REINFORCED_SMASH->300;case IGNIS_ULTIMATE->400;case IGNIS_FLAME_STRIKE->120;case IGNIS_FIREBALL->40;case IGNIS_ABYSS_FIREBALL->80;case IGNIS_BLADE_FIREBALLS->120;case IGNIS_AIR_SMASH->160;case IGNIS_SWORD_COMBO->240;case GUARDIAN_UPPERCUT->200;case GUARDIAN_BLINK->120;case GUARDIAN_AIR_STRIKE->240;case GUARDIAN_ROCKET_PUNCH->100;case GUARDIAN_BULLET->0;case VOID_VORTEX->300;case GUARDIAN_STOMP->200;case GRAVITY_PUNCH->80;case HARBINGER_CHARGE->80;case HOWITZER_VOLLEY->80;case MISSILE_SALVO->60;case WITHER_SMOKE->20;case LASER_BOLT->0;case WITHER_MISSILE->60;case HOMING_MISSILE->80;case WITHER_HOWITZER->160;case DEATH_LASER->200;case CERAUNUS->40;case WAVE->100;case FLARE_BOMB->160;case LAVA_BOMB->100;case EARTHQUAKE->160;case FISSURE->200;case FLAME_RING->200;case SHOULDER_CHECK->80;case LAVA_ABSORPTION->400;case MONSTROUS->350;case WATER_SPEAR->40;case LIGHTNING_SPEAR->60;case STORM_SERPENT->120;case LIGHTNING_STORM->80;case LIGHTNING_FIELD->100;case STORM_BURST->240;case SPARK->60;case THUNDER_CLOUD->400;case LIGHTNING_ROW->200;case THUNDER_COMBO->360;case ANCHOR_SPIN->240;case ANCHOR_LEAP->300;default->0;};
    }
    public static int cooldownTicks(Ability ability) {
        return SPEC.isLoaded()?COOLDOWN_TICKS.get(ability).get():defaultCooldown(ability);
    }
    public static boolean cooldownsEnabled() { return !SPEC.isLoaded() || COOLDOWNS.get(); }
    public static boolean creativeCooldown() { return SPEC.isLoaded() && CREATIVE_COOLDOWN.get(); }
    public static double quakeRadius() {return SPEC.isLoaded()?QUAKE_RADIUS.get():6.25;}
    public static float absorptionHeal() {return SPEC.isLoaded()?ABSORPTION_HEAL.get().floatValue():15;}
    public static double chargeSpeed() {return SPEC.isLoaded()?CHARGE_SPEED.get():.75;}
    public static int lavaLifetime() {return SPEC.isLoaded()?LAVA_LIFETIME.get():200;}
    public static int fractureDuration() {return SPEC.isLoaded()?FRACTURE_DURATION.get():100;}
    public static int lavaWalkingDuration() {return SPEC.isLoaded()?LAVA_WALKING_DURATION.get():600;}
    private static float scyllaValue(ModConfigSpec.DoubleValue value,double nativeValue) {
        double configured=SPEC.isLoaded()?value.get():-1;return (float)(configured<0?nativeValue:configured);
    }
    public static float scyllaSpearDamage() {return scyllaValue(SCYLLA_SPEAR,com.github.L_Ender.cataclysm.config.CMCommonConfig.Scylla.SpearDamage);}
    public static float scyllaStormDamage() {return scyllaValue(SCYLLA_STORM,com.github.L_Ender.cataclysm.config.CMCommonConfig.Scylla.LightningStormDamage);}
    public static float scyllaFieldDamage() {return scyllaValue(SCYLLA_FIELD,com.github.L_Ender.cataclysm.config.CMCommonConfig.Scylla.LightningAreaDamage);}
    public static float scyllaSnakeDamage() {return scyllaValue(SCYLLA_SNAKE,com.github.L_Ender.cataclysm.config.CMCommonConfig.Scylla.SnakeDamage);}
    public static float scyllaHpDamage() {return scyllaValue(SCYLLA_HP,com.github.L_Ender.cataclysm.config.CMCommonConfig.Scylla.StormHpDamage);}
    public static int scyllaStormDelay() {return SPEC.isLoaded()?SCYLLA_DELAY.get():10;}
    public static int scyllaFieldDuration() {return SPEC.isLoaded()?SCYLLA_FIELD_DURATION.get():160;}
    public static int scyllaCloudWarmup() {return SPEC.isLoaded()?SCYLLA_CLOUD_WARMUP.get():40;}
    public static int waterWalkingDuration() {return SPEC.isLoaded()?WATER_WALKING_DURATION.get():600;}
    public static int anchorRange() {return SPEC.isLoaded()?ANCHOR_RANGE.get():64;}
    public static double anchorPullSpeed() {return SPEC.isLoaded()?ANCHOR_PULL_SPEED.get():2;}
    public static boolean harbingerTerrainFire() {return SPEC.isLoaded() && HARBINGER_FIRE.get();}
    public static int harbingerRange() {return SPEC.isLoaded()?HARBINGER_RANGE.get():48;}
    public static boolean ignisTerrainFire(){return SPEC.isLoaded() && IGNIS_FIRE.get();}
    public static int guardianActionRange(){return SPEC.isLoaded()?GUARDIAN_ACTION_RANGE.get():32;}
    public static int guardianBulletDelay(){return SPEC.isLoaded()?GUARDIAN_BULLET_DELAY.get():30;}
    public static int guardianVortexLifetime(){return SPEC.isLoaded()?GUARDIAN_VORTEX_LIFETIME.get():300;}
    public static double damageAugmentFraction(){return SPEC.isLoaded()?AUGMENT_FRACTION.get():.20;}
    public static double damageAugmentMin(){return SPEC.isLoaded()?AUGMENT_MIN.get():2;}
    public static double damageAugmentMax(){return SPEC.isLoaded()?AUGMENT_MAX.get():10;}
    private CataclysmConfig() {}
}
