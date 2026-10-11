package dev.cataclysmnouveau;
import net.neoforged.neoforge.common.ModConfigSpec;
import java.util.EnumMap;
public final class CataclysmConfig {
    public static final ModConfigSpec SPEC;
    private static final EnumMap<Ability, ModConfigSpec.IntValue> COSTS = new EnumMap<>(Ability.class);
    private static final ModConfigSpec.IntValue AMPLIFY_COST, BOLT_WARMUP;
    private static final ModConfigSpec.DoubleValue BOLT_DAMAGE;
    static {
        var b = new ModConfigSpec.Builder(); b.push("mana");
        for (var a : Ability.values()) COSTS.put(a, b.comment("Ars mana per actual launch or repeated impact.").defineInRange(a.id, a.mana, 0, 100000));
        AMPLIFY_COST = b.defineInRange("per_amplify", 10, 0, 100000); b.pop();
        b.push("bolt_strike");
        BOLT_DAMAGE = b.comment("Addon balance value for native bolt entity base damage.").defineInRange("damage", 16.0, 0, 100000);
        BOLT_WARMUP = b.comment("Native warmup ticks; damage follows 12 ticks later.").defineInRange("warmup_ticks", 34, 0, 1200);
        b.pop(); SPEC=b.build();
    }
    public static int cost(Ability a) { return SPEC.isLoaded()?COSTS.get(a).get():a.mana; }
    public static int amplifyCost() { return SPEC.isLoaded()?AMPLIFY_COST.get():10; }
    public static float boltDamage() { return SPEC.isLoaded()?BOLT_DAMAGE.get().floatValue():16; }
    public static int boltWarmup() { return SPEC.isLoaded()?BOLT_WARMUP.get():34; }
    private CataclysmConfig() {}
}
