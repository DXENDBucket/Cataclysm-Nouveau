package dev.cataclysmnouveau;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Local visual preferences and opt-in diagnostics; never changes gameplay. */
public final class CataclysmClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue VORTEX_DIAGNOSTICS;
    private static final ModConfigSpec.BooleanValue IGNIS_PHANTOM,IGNIS_ACTIONS,SCYLLA_ACTIONS,HARBINGER_ACTIONS,GUARDIAN_ACTIONS,MONSTROSITY_ACTIONS;
    private static final ModConfigSpec.DoubleValue PHANTOM_OPACITY,FIRST_PERSON_OPACITY,PHANTOM_SCALE;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("diagnostics");
        VORTEX_DIAGNOSTICS = builder.comment(
            "Log bounded client lifecycle and native renderer observations for Void Vortices.",
            "Diagnostic only: does not disable optimizers or change vortex behavior.",
            "At most 8 vortices per client world; rejoin to start a new capture.")
            .define("vortex", false);
        builder.pop();
        builder.push("action_phantoms");
        IGNIS_PHANTOM=builder.comment("Visual-only native Ignis Sword Dance phantom, for entity and position anchored actions.").define("ignis_sword_dance",true);
        IGNIS_ACTIONS=builder.comment("Visual-only native Ignis smash, earth shudders and ultimate phantoms.").define("ignis_actions",true);
        SCYLLA_ACTIONS=builder.comment("Native Scylla thunder combo, anchor spin and leap phantoms.").define("scylla_actions",true);
        HARBINGER_ACTIONS=builder.comment("Native Harbinger charge phantom.").define("harbinger_actions",true);
        GUARDIAN_ACTIONS=builder.comment("Native Ender Guardian punch, uppercut, blink and air strike phantoms.").define("guardian_actions",true);
        MONSTROSITY_ACTIONS=builder.comment("Native Monstrosity shoulder check phantom.").define("monstrosity_actions",true);
        PHANTOM_OPACITY=builder.comment("Opacity of all native model layers. Does not change damage or attack range.").defineInRange("opacity",.28,0,1);
        FIRST_PERSON_OPACITY=builder.comment("Lower opacity when the local player is the executor and the camera is first person.").defineInRange("first_person_opacity",.10,0,1);
        PHANTOM_SCALE=builder.comment("Visual scale only; 1 keeps the original Boss weapon reach. Does not change hitboxes.").defineInRange("scale",1.0,.25,2);
        builder.pop();
        SPEC = builder.build();
    }
    public static boolean ignisPhantom(){return !SPEC.isLoaded() || IGNIS_PHANTOM.get();}
    public static boolean ignisPhantom(int animation){return animation<=2?ignisPhantom():!SPEC.isLoaded() || IGNIS_ACTIONS.get();}
    public static boolean bodyPhantom(dev.cataclysmnouveau.casting.BodyPhantomAnimations.Kind kind){
        if(kind==null)return false;
        return !SPEC.isLoaded() || switch(kind){
            case SCYLLA -> SCYLLA_ACTIONS.get();case HARBINGER -> HARBINGER_ACTIONS.get();
            case GUARDIAN -> GUARDIAN_ACTIONS.get();case MONSTROSITY -> MONSTROSITY_ACTIONS.get();
        };
    }
    public static float phantomOpacity(boolean firstPerson){return SPEC.isLoaded()?(firstPerson?FIRST_PERSON_OPACITY:PHANTOM_OPACITY).get().floatValue():firstPerson?.10F:.28F;}
    public static float phantomScale(){return SPEC.isLoaded()?PHANTOM_SCALE.get().floatValue():1;}
    private CataclysmClientConfig() {}
}
