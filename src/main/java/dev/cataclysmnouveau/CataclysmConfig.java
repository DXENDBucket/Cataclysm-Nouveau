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
    private CataclysmConfig() {}
}
