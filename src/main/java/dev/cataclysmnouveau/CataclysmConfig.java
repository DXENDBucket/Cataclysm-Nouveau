package dev.cataclysmnouveau;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.EnumMap;
public final class CataclysmConfig {
    public static final ModConfigSpec SPEC;
    private static final EnumMap<Ability, ModConfigSpec.IntValue> COSTS = new EnumMap<>(Ability.class);
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
        SCYLLA_FIELD_DURATION=b.comment("Standalone electric field duration before duration augments; radius shrinks across this interval.").defineInRange("field_duration_ticks",160,20,1200);b.pop();
        b.push("cooldowns");
        COOLDOWNS=b.comment("Gate new active uses through Conflux; existing trajectories keep running.").define("enabled",true);
        CREATIVE_COOLDOWN=b.define("creative_cooldown",false);
        b.push("ticks");
        for(var a:Ability.values()) COOLDOWN_TICKS.put(a,b.defineInRange(a.id,defaultCooldown(a),0,72000));
        b.pop();b.pop();
        SPEC=b.build();
    }
    public static int cost(Ability a) { return SPEC.isLoaded()?COSTS.get(a).get():a.mana; }
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
        return switch(ability) {case CERAUNUS->40;case WAVE->100;case FLARE_BOMB->160;case LAVA_BOMB->100;case EARTHQUAKE->160;case FISSURE->200;case FLAME_RING->200;case SHOULDER_CHECK->80;case LAVA_ABSORPTION->400;case MONSTROUS->350;case WATER_SPEAR->40;case LIGHTNING_SPEAR->60;case STORM_SERPENT->120;case LIGHTNING_STORM->80;case LIGHTNING_FIELD->100;case STORM_BURST->240;default->0;};
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
    private CataclysmConfig() {}
}
