package dev.cataclysmnouveau;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Local, opt-in diagnostics; never changes gameplay or rendering decisions. */
public final class CataclysmClientConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue VORTEX_DIAGNOSTICS;
    static {
        var builder = new ModConfigSpec.Builder();
        builder.push("diagnostics");
        VORTEX_DIAGNOSTICS = builder.comment(
            "Log bounded client lifecycle and native renderer observations for Void Vortices.",
            "Diagnostic only: does not disable optimizers or change vortex behavior.",
            "At most 8 vortices per client world; rejoin to start a new capture.")
            .define("vortex", false);
        builder.pop();
        SPEC = builder.build();
    }
    private CataclysmClientConfig() {}
}
